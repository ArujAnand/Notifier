package com.investmentsentinel.service.market;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Immutable snapshot of market data with audit metadata and staleness tracking.
 */
public record MarketDataSnapshot(
        String asset,
        BigDecimal value,
        OffsetDateTime timestamp,
        String provider,
        boolean fresh
) {
    public static MarketDataSnapshot fresh(String asset, BigDecimal value, OffsetDateTime timestamp, String provider) {
        return new MarketDataSnapshot(asset, value, timestamp, provider, true);
    }

    public static MarketDataSnapshot stale(String asset, BigDecimal value, OffsetDateTime timestamp, String provider) {
        return new MarketDataSnapshot(asset, value, timestamp, provider, false);
    }
}
