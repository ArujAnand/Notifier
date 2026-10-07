package com.investmentsentinel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mockito;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.investmentsentinel.config.AppProperties;
import com.investmentsentinel.domain.Investment;
import com.investmentsentinel.domain.MarketReference;
import com.investmentsentinel.domain.TriggerEvent;
import com.investmentsentinel.repository.HistoricalInvestmentRepository;
import com.investmentsentinel.repository.InvestmentRepository;
import com.investmentsentinel.repository.MarketReferenceRepository;
import com.investmentsentinel.repository.MarketSnapshotRepository;
import com.investmentsentinel.repository.NotificationRepository;
import com.investmentsentinel.repository.TriggerEventRepository;
import com.investmentsentinel.service.market.MarketDataService;
import com.investmentsentinel.service.market.MarketDataSnapshot;
import com.investmentsentinel.service.monitoring.InvestmentMonitoringService;
import com.investmentsentinel.service.sms.SmsService;

class ThresholdTriggerTest {

    private InvestmentRepository investmentRepository;
    private MarketReferenceRepository marketReferenceRepository;
    private MarketSnapshotRepository marketSnapshotRepository;
    private TriggerEventRepository triggerEventRepository;
    private NotificationRepository notificationRepository;
    private HistoricalInvestmentRepository historicalInvestmentRepository;
    private MarketDataService marketDataService;
    private SmsService smsService;
    private AppProperties properties;
    private InvestmentMonitoringService service;

    @BeforeEach
    void setUp() {
        investmentRepository = Mockito.mock(InvestmentRepository.class);
        marketReferenceRepository = Mockito.mock(MarketReferenceRepository.class);
        marketSnapshotRepository = Mockito.mock(MarketSnapshotRepository.class);
        triggerEventRepository = Mockito.mock(TriggerEventRepository.class);
        notificationRepository = Mockito.mock(NotificationRepository.class);
        historicalInvestmentRepository = Mockito.mock(HistoricalInvestmentRepository.class);
        marketDataService = Mockito.mock(MarketDataService.class);
        smsService = Mockito.mock(SmsService.class);

        properties = new AppProperties();
        properties.getMonitoring().setEarlyDrawdownAlerts(true);
        properties.getMonitoring().setReviewIntervalMonths(3);

        service = new InvestmentMonitoringService(
                investmentRepository,
                marketReferenceRepository,
                marketSnapshotRepository,
                triggerEventRepository,
                notificationRepository,
                historicalInvestmentRepository,
                marketDataService,
                smsService,
                properties
        );
    }

    @Test
    @DisplayName("Crosses -5% threshold for the first time: generates trigger event and early alert SMS")
    void testFirstThresholdCrossing() {
        Investment inv = new Investment("ATLAS GLOBAL", "USD");
        inv.setId(1L);
        inv.setLastInvestmentDate(LocalDate.of(2026, 1, 10));
        inv.setNextReviewDate(LocalDate.of(2026, 4, 10)); // Review is in future

        MarketReference ref = new MarketReference(1L, "SP500", new BigDecimal("10000.00"), LocalDate.of(2026, 1, 10), "Base");
        when(marketReferenceRepository.findFirstByInvestmentIdOrderByCreatedAtDesc(1L)).thenReturn(Optional.of(ref));

        // Drawdown is -6.00% (value: 9400.00)
        MarketDataSnapshot snapshot = MarketDataSnapshot.fresh("SP500", new BigDecimal("9400.00"), OffsetDateTime.now(), "MOCK");
        when(marketDataService.getSp500Level()).thenReturn(Optional.of(snapshot));
        when(triggerEventRepository.findFirstByInvestmentIdAndThresholdOrderByTriggeredAtDesc(eq(1L), any()))
                .thenReturn(Optional.empty());

        service.evaluateInvestment(inv, LocalDate.of(2026, 2, 1));

        // Verify trigger event saved for -5.00%
        verify(triggerEventRepository).save(any(TriggerEvent.class));
        // Verify SMS alert dispatched
        verify(smsService, times(1)).sendNotification(eq(1L), eq("THRESHOLD_ALERT"), eq(new BigDecimal("-5.0")), any());
    }

    @Test
    @DisplayName("Market remains at -7%: duplicate notification is avoided (no repeated SMS)")
    void testNoDuplicateAlertWhenMarketStaysInDrawdown() {
        Investment inv = new Investment("ATLAS GLOBAL", "USD");
        inv.setId(1L);
        inv.setLastInvestmentDate(LocalDate.of(2026, 1, 10));
        inv.setNextReviewDate(LocalDate.of(2026, 4, 10));

        MarketReference ref = new MarketReference(1L, "SP500", new BigDecimal("10000.00"), LocalDate.of(2026, 1, 10), "Base");
        when(marketReferenceRepository.findFirstByInvestmentIdOrderByCreatedAtDesc(1L)).thenReturn(Optional.of(ref));

        // Market at -7% (9300.00)
        MarketDataSnapshot snapshot = MarketDataSnapshot.fresh("SP500", new BigDecimal("9300.00"), OffsetDateTime.now(), "MOCK");
        when(marketDataService.getSp500Level()).thenReturn(Optional.of(snapshot));

        // Prior alert was already recorded
        TriggerEvent existingAlert = new TriggerEvent(1L, new BigDecimal("-5.0"), new BigDecimal("-6.0"),
                new BigDecimal("9400.00"), new BigDecimal("10000.00"), "THRESHOLD_ALERT");
        when(triggerEventRepository.findFirstByInvestmentIdAndThresholdOrderByTriggeredAtDesc(1L, new BigDecimal("-5.0")))
                .thenReturn(Optional.of(existingAlert));

        service.evaluateInvestment(inv, LocalDate.of(2026, 2, 2));

        // Must NOT send duplicate SMS
        verify(smsService, never()).sendNotification(any(), eq("THRESHOLD_ALERT"), eq(new BigDecimal("-5.0")), any());
    }

    @Test
    @DisplayName("Market subsequently drops to -16%: new -15% threshold alert is sent")
    void testSecondThresholdCrossing() {
        Investment inv = new Investment("ATLAS GLOBAL", "USD");
        inv.setId(1L);
        inv.setLastInvestmentDate(LocalDate.of(2026, 1, 10));
        inv.setNextReviewDate(LocalDate.of(2026, 4, 10));

        MarketReference ref = new MarketReference(1L, "SP500", new BigDecimal("10000.00"), LocalDate.of(2026, 1, 10), "Base");
        when(marketReferenceRepository.findFirstByInvestmentIdOrderByCreatedAtDesc(1L)).thenReturn(Optional.of(ref));

        // Market at -16% (8400.00)
        MarketDataSnapshot snapshot = MarketDataSnapshot.fresh("SP500", new BigDecimal("8400.00"), OffsetDateTime.now(), "MOCK");
        when(marketDataService.getSp500Level()).thenReturn(Optional.of(snapshot));

        // -5% was already triggered
        TriggerEvent alert5 = new TriggerEvent(1L, new BigDecimal("-5.0"), new BigDecimal("-6.0"),
                new BigDecimal("9400.00"), new BigDecimal("10000.00"), "THRESHOLD_ALERT");
        when(triggerEventRepository.findFirstByInvestmentIdAndThresholdOrderByTriggeredAtDesc(1L, new BigDecimal("-5.0")))
                .thenReturn(Optional.of(alert5));
        // -15% was NOT triggered yet
        when(triggerEventRepository.findFirstByInvestmentIdAndThresholdOrderByTriggeredAtDesc(1L, new BigDecimal("-15.0")))
                .thenReturn(Optional.empty());

        service.evaluateInvestment(inv, LocalDate.of(2026, 2, 10));

        // Verifies -15% alert dispatched
        verify(smsService, times(1)).sendNotification(eq(1L), eq("THRESHOLD_ALERT"), eq(new BigDecimal("-15.0")), any());
    }

    @Test
    @DisplayName("Market recovers above threshold and drops again: recrossing is properly detected")
    void testRecoveryAndRecrossing() {
        Investment inv = new Investment("ATLAS GLOBAL", "USD");
        inv.setId(1L);
        inv.setLastInvestmentDate(LocalDate.of(2026, 1, 10));
        inv.setNextReviewDate(LocalDate.of(2026, 4, 10));

        MarketReference ref = new MarketReference(1L, "SP500", new BigDecimal("10000.00"), LocalDate.of(2026, 1, 10), "Base");
        when(marketReferenceRepository.findFirstByInvestmentIdOrderByCreatedAtDesc(1L)).thenReturn(Optional.of(ref));

        // Market recovered previously (last event was THRESHOLD_RECOVERY)
        TriggerEvent recoveryEvent = new TriggerEvent(1L, new BigDecimal("-5.0"), new BigDecimal("-3.0"),
                new BigDecimal("9700.00"), new BigDecimal("10000.00"), "THRESHOLD_RECOVERY");
        when(triggerEventRepository.findFirstByInvestmentIdAndThresholdOrderByTriggeredAtDesc(1L, new BigDecimal("-5.0")))
                .thenReturn(Optional.of(recoveryEvent));

        // Now market drops again to -6.5% (9350.00)
        MarketDataSnapshot snapshot = MarketDataSnapshot.fresh("SP500", new BigDecimal("9350.00"), OffsetDateTime.now(), "MOCK");
        when(marketDataService.getSp500Level()).thenReturn(Optional.of(snapshot));

        service.evaluateInvestment(inv, LocalDate.of(2026, 3, 1));

        // Should trigger again because it recovered previously!
        verify(smsService, times(1)).sendNotification(eq(1L), eq("THRESHOLD_ALERT"), eq(new BigDecimal("-5.0")), any());
    }
}
