package com.investmentsentinel.service.market;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;

public interface MarketDataService {

    /**
     * Retrieves current level for S&P 500 (ticker: ^GSPC).
     */
    Optional<MarketDataSnapshot> getSp500Level();

    /**
     * Retrieves current gold benchmark price (INR/gram or USD/oz converted).
     */
    Optional<MarketDataSnapshot> getGoldPrice();

    /**
     * Retrieves current price / NAV for ICICI Prudential Gold ETF (NSE: ICICIGOLD.NS).
     */
    Optional<MarketDataSnapshot> getIciciGoldEtfPrice();

    /**
     * Validates whether market data snapshot is fresh within the configured threshold
     * (taking into account weekend and market holiday closures).
     */
    default boolean isDataFresh(MarketDataSnapshot snapshot, int maxStaleHours) {
        if (snapshot == null || snapshot.timestamp() == null) {
            return false;
        }
        Duration age = Duration.between(snapshot.timestamp(), OffsetDateTime.now());
        return age.toHours() <= maxStaleHours;
    }
}
