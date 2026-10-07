package com.investmentsentinel.service.market;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Mock market data provider for isolated tests, local offline operation,
 * and deterministic simulation.
 */
@Service
@ConditionalOnProperty(name = "app.market-data.provider", havingValue = "mock")
public class MockMarketDataService implements MarketDataService {

    private BigDecimal mockSp500 = new BigDecimal("5510.00"); // e.g. -5.00% from 5800
    private BigDecimal mockGoldPrice = new BigDecimal("68.15"); // e.g. -6.0% from 72.50
    private BigDecimal mockGoldEtfPrice = new BigDecimal("68.15");
    private boolean forceStale = false;
    private boolean forceFailure = false;

    @Override
    public Optional<MarketDataSnapshot> getSp500Level() {
        if (forceFailure) return Optional.empty();
        OffsetDateTime timestamp = forceStale ? OffsetDateTime.now().minusDays(5) : OffsetDateTime.now();
        return Optional.of(new MarketDataSnapshot("SP500", mockSp500, timestamp, "MOCK_PROVIDER", !forceStale));
    }

    @Override
    public Optional<MarketDataSnapshot> getGoldPrice() {
        if (forceFailure) return Optional.empty();
        OffsetDateTime timestamp = forceStale ? OffsetDateTime.now().minusDays(5) : OffsetDateTime.now();
        return Optional.of(new MarketDataSnapshot("GOLD_BENCHMARK", mockGoldPrice, timestamp, "MOCK_PROVIDER", !forceStale));
    }

    @Override
    public Optional<MarketDataSnapshot> getIciciGoldEtfPrice() {
        if (forceFailure) return Optional.empty();
        OffsetDateTime timestamp = forceStale ? OffsetDateTime.now().minusDays(5) : OffsetDateTime.now();
        return Optional.of(new MarketDataSnapshot("GOLD_ETF", mockGoldEtfPrice, timestamp, "MOCK_PROVIDER", !forceStale));
    }

    public void setMockSp500(BigDecimal mockSp500) { this.mockSp500 = mockSp500; }
    public void setMockGoldPrice(BigDecimal mockGoldPrice) { this.mockGoldPrice = mockGoldPrice; }
    public void setMockGoldEtfPrice(BigDecimal mockGoldEtfPrice) { this.mockGoldEtfPrice = mockGoldEtfPrice; }
    public void setForceStale(boolean forceStale) { this.forceStale = forceStale; }
    public void setForceFailure(boolean forceFailure) { this.forceFailure = forceFailure; }
}
