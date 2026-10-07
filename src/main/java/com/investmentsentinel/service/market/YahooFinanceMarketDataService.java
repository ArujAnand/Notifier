package com.investmentsentinel.service.market;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.investmentsentinel.config.AppProperties;

/**
 * Real Market Data service backed by Yahoo Finance public chart endpoints.
 * Free, requires no API key, and directly tracks:
 * - ^GSPC (S&P 500 Index)
 * - ICICIGOLD.NS (ICICI Prudential Gold ETF on National Stock Exchange of India)
 * - GC=F (COMEX Gold Futures)
 */
@Service
@ConditionalOnProperty(name = "app.market-data.provider", havingValue = "yahoo", matchIfMissing = true)
public class YahooFinanceMarketDataService implements MarketDataService {

    private static final Logger log = LoggerFactory.getLogger(YahooFinanceMarketDataService.class);
    private static final String YAHOO_URL_TEMPLATE = "https://query1.finance.yahoo.com/v8/finance/chart/{ticker}?interval=1d&range=5d";

    private final RestClient restClient;
    private final AppProperties properties;

    public YahooFinanceMarketDataService(RestClient restClient, AppProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    @Override
    public Optional<MarketDataSnapshot> getSp500Level() {
        return fetchPrice("^GSPC", "SP500");
    }

    @Override
    public Optional<MarketDataSnapshot> getGoldPrice() {
        return fetchPrice("GC=F", "GOLD_BENCHMARK");
    }

    @Override
    public Optional<MarketDataSnapshot> getIciciGoldEtfPrice() {
        // Preferred: Direct ICICI Prudential Gold ETF on NSE
        Optional<MarketDataSnapshot> etfPrice = fetchPrice("ICICIGOLD.NS", "GOLD_ETF");
        if (etfPrice.isPresent()) {
            return etfPrice;
        }
        log.warn("ICICIGOLD.NS unavailable from primary source, falling back to Gold Futures (GC=F)");
        return getGoldPrice();
    }

    private Optional<MarketDataSnapshot> fetchPrice(String ticker, String assetName) {
        try {
            log.info("Fetching market data for {} ({}) from Yahoo Finance", assetName, ticker);
            JsonNode root = restClient.get()
                    .uri(YAHOO_URL_TEMPLATE, ticker)
                    .retrieve()
                    .body(JsonNode.class);

            if (root == null || !root.has("chart") || !root.get("chart").has("result")) {
                log.warn("Empty or invalid response from Yahoo Finance for ticker {}", ticker);
                return Optional.empty();
            }

            JsonNode result = root.get("chart").get("result").get(0);
            JsonNode meta = result.get("meta");

            BigDecimal price = null;
            if (meta.hasNonNull("regularMarketPrice")) {
                price = BigDecimal.valueOf(meta.get("regularMarketPrice").asDouble())
                        .setScale(4, RoundingMode.HALF_UP);
            }

            long timestampSeconds = meta.hasNonNull("regularMarketTime")
                    ? meta.get("regularMarketTime").asLong()
                    : Instant.now().getEpochSecond();

            OffsetDateTime timestamp = Instant.ofEpochSecond(timestampSeconds).atOffset(ZoneOffset.UTC);

            if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("Price extracted was null or zero for ticker {}", ticker);
                return Optional.empty();
            }

            boolean fresh = isDataFresh(new MarketDataSnapshot(assetName, price, timestamp, "YAHOO_FINANCE", true),
                    properties.getMarketData().getStaleThresholdHours());

            log.info("Successfully fetched {} ({}): {} as of {} (Fresh: {})",
                    assetName, ticker, price, timestamp, fresh);

            return Optional.of(new MarketDataSnapshot(assetName, price, timestamp, "YAHOO_FINANCE", fresh));
        } catch (Exception e) {
            log.error("Failed to retrieve market data for {} ({}): {}", assetName, ticker, e.getMessage());
            return Optional.empty();
        }
    }
}
