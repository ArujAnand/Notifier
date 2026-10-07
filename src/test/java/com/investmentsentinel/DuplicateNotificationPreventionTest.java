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
import com.investmentsentinel.domain.HistoricalInvestment;
import com.investmentsentinel.domain.Investment;
import com.investmentsentinel.domain.MarketReference;
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

class DuplicateNotificationPreventionTest {

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
    @DisplayName("Scheduled review is idempotent: running twice on the same day sends only one SMS")
    void testScheduledReviewIdempotency() {
        Investment inv = new Investment("ATLAS GLOBAL", "USD");
        inv.setId(1L);
        inv.setLastInvestmentDate(LocalDate.of(2026, 1, 10));
        inv.setNextReviewDate(LocalDate.of(2026, 4, 10)); // Review due on or after April 10

        LocalDate reviewDay = LocalDate.of(2026, 4, 10);

        MarketReference ref = new MarketReference(1L, "SP500", new BigDecimal("10000.00"), LocalDate.of(2026, 1, 10), "Initial");
        when(marketReferenceRepository.findFirstByInvestmentIdOrderByCreatedAtDesc(1L)).thenReturn(Optional.of(ref));

        MarketDataSnapshot snapshot = MarketDataSnapshot.fresh("SP500", new BigDecimal("9400.00"), OffsetDateTime.now(), "MOCK");
        when(marketDataService.getSp500Level()).thenReturn(Optional.of(snapshot));

        // First run: notification has not been sent yet
        when(notificationRepository.existsByInvestmentIdAndNotificationTypeAndSentAtAfter(eq(1L), eq("SCHEDULED_REVIEW"), any()))
                .thenReturn(false);

        service.evaluateInvestment(inv, reviewDay);
        verify(smsService, times(1)).sendNotification(eq(1L), eq("SCHEDULED_REVIEW"), any(), any());

        // Second run on the same day: repository reports notification already exists
        when(notificationRepository.existsByInvestmentIdAndNotificationTypeAndSentAtAfter(eq(1L), eq("SCHEDULED_REVIEW"), any()))
                .thenReturn(true);

        service.evaluateInvestment(inv, reviewDay);
        // Still only 1 time in total!
        verify(smsService, times(1)).sendNotification(eq(1L), eq("SCHEDULED_REVIEW"), any(), any());
    }

    @Test
    @DisplayName("Recording an investment preserves reference and updates calendar review date")
    void testRecordInvestmentPreservesReference() {
        Investment inv = new Investment("ATLAS GLOBAL", "USD");
        inv.setId(1L);
        inv.setLastInvestmentDate(LocalDate.of(2026, 1, 10));
        inv.setNextReviewDate(LocalDate.of(2026, 4, 10));

        when(investmentRepository.findById(1L)).thenReturn(Optional.of(inv));

        LocalDate newDate = LocalDate.of(2026, 4, 10);
        service.recordInvestment(1L, new BigDecimal("75.00"), newDate, "Contribution at -15%");

        assertThat(inv.getLastInvestmentAmount()).isEqualByComparingTo(new BigDecimal("75.00"));
        assertThat(inv.getLastInvestmentDate()).isEqualTo(newDate);
        assertThat(inv.getNextReviewDate()).isEqualTo(LocalDate.of(2026, 7, 10)); // Exactly 3 calendar months later

        // Verify saved in history table
        verify(historicalInvestmentRepository).save(any(HistoricalInvestment.class));
        // Verify market reference repository was NEVER modified
        verify(marketReferenceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Resetting reference creates an explicit audit entry with reason")
    void testResetReferenceCreatesAuditEntry() {
        Investment inv = new Investment("ATLAS GLOBAL", "USD");
        inv.setId(1L);
        when(investmentRepository.findById(1L)).thenReturn(Optional.of(inv));

        LocalDate resetDate = LocalDate.of(2026, 5, 1);
        service.resetReference(1L, new BigDecimal("6200.00"), resetDate, "Manual annual portfolio recalibration");

        verify(marketReferenceRepository).save(Mockito.argThat(ref ->
                ref.getInvestmentId().equals(1L) &&
                ref.getReferenceValue().compareTo(new BigDecimal("6200.00")) == 0 &&
                ref.getReferenceDate().equals(resetDate) &&
                ref.getReason().contains("Manual annual")
        ));
    }
}
