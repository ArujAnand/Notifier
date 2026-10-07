package com.investmentsentinel;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.investmentsentinel.service.market.MarketDataService;
import com.investmentsentinel.service.market.MarketDataSnapshot;

class MarketDataStalenessTest {

    private final MarketDataService service = new MarketDataService() {
        @Override
        public java.util.Optional<MarketDataSnapshot> getSp500Level() { return java.util.Optional.empty(); }
        @Override
        public java.util.Optional<MarketDataSnapshot> getGoldPrice() { return java.util.Optional.empty(); }
        @Override
        public java.util.Optional<MarketDataSnapshot> getIciciGoldEtfPrice() { return java.util.Optional.empty(); }
    };

    @Test
    @DisplayName("Data from Friday close on Monday morning (60 hours old) is recognized as fresh")
    void testWeekendMarketFreshness() {
        // 60 hours ago (Friday evening to Monday 8 AM)
        OffsetDateTime fridayClose = OffsetDateTime.now().minusHours(60);
        MarketDataSnapshot snapshot = new MarketDataSnapshot("SP500", new BigDecimal("5800.00"), fridayClose, "YAHOO", true);

        boolean fresh = service.isDataFresh(snapshot, 72);
        assertThat(fresh).isTrue();
    }

    @Test
    @DisplayName("Data older than 72 hours (e.g. 80 hours) is rejected as stale to prevent false alerts")
    void testStaleDataRejection() {
        OffsetDateTime fourDaysAgo = OffsetDateTime.now().minusHours(80);
        MarketDataSnapshot snapshot = new MarketDataSnapshot("SP500", new BigDecimal("5800.00"), fourDaysAgo, "YAHOO", true);

        boolean fresh = service.isDataFresh(snapshot, 72);
        assertThat(fresh).isFalse();
    }

    @Test
    @DisplayName("Null snapshot or missing timestamp is safely reported as not fresh")
    void testNullSnapshotHandling() {
        assertThat(service.isDataFresh(null, 72)).isFalse();
        MarketDataSnapshot nullTimestamp = new MarketDataSnapshot("SP500", new BigDecimal("5800.00"), null, "YAHOO", true);
        assertThat(service.isDataFresh(nullTimestamp, 72)).isFalse();
    }
}
