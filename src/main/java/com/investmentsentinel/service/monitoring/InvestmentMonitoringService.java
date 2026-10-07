package com.investmentsentinel.service.monitoring;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.investmentsentinel.config.AppProperties;
import com.investmentsentinel.domain.HistoricalInvestment;
import com.investmentsentinel.domain.Investment;
import com.investmentsentinel.domain.MarketReference;
import com.investmentsentinel.domain.MarketSnapshot;
import com.investmentsentinel.domain.TriggerEvent;
import com.investmentsentinel.repository.HistoricalInvestmentRepository;
import com.investmentsentinel.repository.InvestmentRepository;
import com.investmentsentinel.repository.MarketReferenceRepository;
import com.investmentsentinel.repository.MarketSnapshotRepository;
import com.investmentsentinel.repository.NotificationRepository;
import com.investmentsentinel.repository.TriggerEventRepository;
import com.investmentsentinel.service.market.MarketDataService;
import com.investmentsentinel.service.market.MarketDataSnapshot;
import com.investmentsentinel.service.sms.SmsService;

@Service
public class InvestmentMonitoringService {

    private static final Logger log = LoggerFactory.getLogger(InvestmentMonitoringService.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

    private final InvestmentRepository investmentRepository;
    private final MarketReferenceRepository marketReferenceRepository;
    private final MarketSnapshotRepository marketSnapshotRepository;
    private final TriggerEventRepository triggerEventRepository;
    private final NotificationRepository notificationRepository;
    private final HistoricalInvestmentRepository historicalInvestmentRepository;
    private final MarketDataService marketDataService;
    private final SmsService smsService;
    private final AppProperties properties;

    public InvestmentMonitoringService(
            InvestmentRepository investmentRepository,
            MarketReferenceRepository marketReferenceRepository,
            MarketSnapshotRepository marketSnapshotRepository,
            TriggerEventRepository triggerEventRepository,
            NotificationRepository notificationRepository,
            HistoricalInvestmentRepository historicalInvestmentRepository,
            MarketDataService marketDataService,
            SmsService smsService,
            AppProperties properties) {
        this.investmentRepository = investmentRepository;
        this.marketReferenceRepository = marketReferenceRepository;
        this.marketSnapshotRepository = marketSnapshotRepository;
        this.triggerEventRepository = triggerEventRepository;
        this.notificationRepository = notificationRepository;
        this.historicalInvestmentRepository = historicalInvestmentRepository;
        this.marketDataService = marketDataService;
        this.smsService = smsService;
        this.properties = properties;
    }

    /**
     * Executes the daily monitoring check across all active investments.
     * Guaranteed to be idempotent: running multiple times a day will NOT produce duplicate notifications.
     */
    @Transactional
    public void runDailyEvaluation() {
        LocalDate today = LocalDate.now(properties.getZoneId());
        log.info("Starting daily investment evaluation for date: {} (Timezone: {})", today, properties.getTimezone());

        List<Investment> activeInvestments = investmentRepository.findByActiveTrue();
        for (Investment investment : activeInvestments) {
            try {
                evaluateInvestment(investment, today);
            } catch (Exception e) {
                log.error("Error evaluating investment {}: {}", investment.getName(), e.getMessage(), e);
            }
        }
        log.info("Daily investment evaluation completed successfully.");
    }

    @Transactional
    public void evaluateInvestment(Investment investment, LocalDate today) {
        // 1. Retrieve current reference
        Optional<MarketReference> referenceOpt = marketReferenceRepository
                .findFirstByInvestmentIdOrderByCreatedAtDesc(investment.getId());

        if (referenceOpt.isEmpty()) {
            log.warn("No market reference found for investment '{}'. Skipping.", investment.getName());
            return;
        }
        MarketReference reference = referenceOpt.get();

        // 2. Fetch market data snapshot
        Optional<MarketDataSnapshot> marketSnapshotOpt = fetchMarketSnapshot(investment);
        if (marketSnapshotOpt.isEmpty()) {
            log.warn("Could not retrieve market data for '{}'. Skipping to prevent false triggers.", investment.getName());
            return;
        }

        MarketDataSnapshot marketData = marketSnapshotOpt.get();
        if (!marketData.fresh()) {
            log.warn("Market data for '{}' is stale (timestamp: {}). Skipping to avoid false alerts.",
                    investment.getName(), marketData.timestamp());
            return;
        }

        // Persist market snapshot for audit
        marketSnapshotRepository.save(new MarketSnapshot(
                marketData.asset(),
                marketData.value(),
                marketData.timestamp(),
                marketData.provider()
        ));

        // 3. Calculate drawdown and suggested contribution
        AppProperties.TierConfig tierConfig = getTierConfig(investment);
        DrawdownCalculation calc = DrawdownCalculation.calculate(
                marketData.value(),
                reference.getReferenceValue(),
                tierConfig.getNormalAmount(),
                tierConfig.getDrawdown1(), tierConfig.getAmount1(),
                tierConfig.getDrawdown2(), tierConfig.getAmount2(),
                tierConfig.getDrawdown3(), tierConfig.getAmount3()
        );

        log.info("Investment '{}' [{}] Current: {}, Reference: {}, Drawdown: {}%, Suggested: {} {}",
                investment.getName(), reference.getReferenceType(),
                calc.currentValue(), calc.referenceValue(), calc.drawdownPercentage(),
                investment.getCurrency(), calc.suggestedAmount());

        // 4. Check three-month schedule review
        boolean reviewDue = investment.isReviewDue(today);

        if (reviewDue) {
            handleScheduledReview(investment, calc, today, reference.getReferenceType());
        }

        // 5. Check threshold crossing triggers & recovery
        checkThresholds(investment, calc, tierConfig, today, reference.getReferenceType(), reviewDue);
    }

    private void handleScheduledReview(Investment investment, DrawdownCalculation calc, LocalDate today, String assetType) {
        // Prevent duplicate scheduled review on the same calendar day
        OffsetDateTime startOfDay = today.atStartOfDay(properties.getZoneId()).toOffsetDateTime();
        boolean alreadyNotifiedToday = notificationRepository.existsByInvestmentIdAndNotificationTypeAndSentAtAfter(
                investment.getId(), "SCHEDULED_REVIEW", startOfDay);

        if (alreadyNotifiedToday) {
            log.info("Scheduled review notification for '{}' was already sent today. Skipping duplicate.", investment.getName());
            return;
        }

        String formattedLastDate = investment.getLastInvestmentDate() != null
                ? investment.getLastInvestmentDate().format(DATE_FMT)
                : "N/A";

        String assetLabel = assetType.equalsIgnoreCase("SP500") ? "S&P 500" : "Gold/ETF";

        String message = String.format("""
                %s review due.
                
                Last: %s %s on %s
                
                %s:
                Current: %s
                Reference: %s
                Drawdown: %s%%
                
                Suggested: %s %s
                Reason: %s.
                
                This is a reminder only.
                No trade has been placed.""",
                investment.getName(),
                investment.getCurrency(), investment.getLastInvestmentAmount(), formattedLastDate,
                assetLabel,
                calc.currentValue().stripTrailingZeros().toPlainString(),
                calc.referenceValue().stripTrailingZeros().toPlainString(),
                calc.drawdownPercentage(),
                investment.getCurrency(), calc.suggestedAmount(),
                calc.reason());

        log.info("Dispatching Scheduled Review SMS for {}", investment.getName());
        smsService.sendNotification(investment.getId(), "SCHEDULED_REVIEW", calc.activeThreshold(), message);
    }

    private void checkThresholds(Investment investment, DrawdownCalculation calc, AppProperties.TierConfig tierConfig,
                                 LocalDate today, String assetType, boolean reviewDue) {
        BigDecimal[] thresholds = {
                tierConfig.getDrawdown1().negate(), // -5.00
                tierConfig.getDrawdown2().negate(), // -15.00
                tierConfig.getDrawdown3().negate()  // -25.00
        };

        for (BigDecimal threshold : thresholds) {
            Optional<TriggerEvent> lastTrigger = triggerEventRepository
                    .findFirstByInvestmentIdAndThresholdOrderByTriggeredAtDesc(investment.getId(), threshold);

            boolean currentlyBelowOrAt = calc.drawdownPercentage().compareTo(threshold) <= 0;

            if (currentlyBelowOrAt) {
                // If never triggered or last event was recovery, trigger now!
                if (lastTrigger.isEmpty() || "THRESHOLD_RECOVERY".equals(lastTrigger.get().getTriggerType())) {
                    log.info("Threshold crossing detected for {} at {}% (Current Drawdown: {}%)",
                            investment.getName(), threshold, calc.drawdownPercentage());

                    TriggerEvent triggerEvent = new TriggerEvent(
                            investment.getId(),
                            threshold,
                            calc.drawdownPercentage(),
                            calc.currentValue(),
                            calc.referenceValue(),
                            "THRESHOLD_ALERT"
                    );
                    triggerEventRepository.save(triggerEvent);

                    // Send alert only if early alerts are enabled and review is not due today
                    if (properties.getMonitoring().isEarlyDrawdownAlerts() && !reviewDue) {
                        sendThresholdAlertSms(investment, calc, threshold, today, assetType);
                    }
                }
            } else {
                // Currently above threshold (recovered)
                if (lastTrigger.isPresent() && "THRESHOLD_ALERT".equals(lastTrigger.get().getTriggerType())) {
                    log.info("Market recovered above threshold {}% for {} (Current Drawdown: {}%). Recording recovery event.",
                            threshold, investment.getName(), calc.drawdownPercentage());

                    TriggerEvent recoveryEvent = new TriggerEvent(
                            investment.getId(),
                            threshold,
                            calc.drawdownPercentage(),
                            calc.currentValue(),
                            calc.referenceValue(),
                            "THRESHOLD_RECOVERY"
                    );
                    triggerEventRepository.save(recoveryEvent);
                }
            }
        }
    }

    private void sendThresholdAlertSms(Investment investment, DrawdownCalculation calc, BigDecimal threshold,
                                      LocalDate today, String assetType) {
        String assetLabel = assetType.equalsIgnoreCase("SP500") ? "S&P 500" : "Gold/ETF";
        long daysUntilReview = investment.getNextReviewDate() != null
                ? ChronoUnit.DAYS.between(today, investment.getNextReviewDate())
                : 0;

        String message = String.format("""
                %s market alert:
                
                %s crossed %s%% from your reference.
                Current drawdown: %s%%
                
                Suggested contribution at next review: %s %s.
                Next scheduled review: %d days.
                
                No trade has been placed.""",
                investment.getName(),
                assetLabel, threshold,
                calc.drawdownPercentage(),
                investment.getCurrency(), calc.suggestedAmount(),
                Math.max(0, daysUntilReview));

        log.info("Dispatching Threshold Alert SMS for {} at threshold {}%", investment.getName(), threshold);
        smsService.sendNotification(investment.getId(), "THRESHOLD_ALERT", threshold, message);
    }

    /**
     * Manually records an investment contribution.
     * Updates last investment date & amount, calculates the next review date (plus reviewIntervalMonths),
     * and preserves the contribution in historical_investments table.
     * Critical Rule: Does NOT reset market reference.
     */
    @Transactional
    public void recordInvestment(Long investmentId, BigDecimal amount, LocalDate date, String notes) {
        Investment investment = investmentRepository.findById(investmentId)
                .orElseThrow(() -> new IllegalArgumentException("Investment not found with ID: " + investmentId));

        int intervalMonths = properties.getMonitoring().getReviewIntervalMonths();
        investment.recordInvestment(amount, date, intervalMonths);
        investmentRepository.save(investment);

        HistoricalInvestment history = new HistoricalInvestment(investmentId, amount, investment.getCurrency(), date, notes);
        historicalInvestmentRepository.save(history);

        log.info("Recorded investment for '{}': {} {}, Next review: {} (Reference unchanged)",
                investment.getName(), investment.getCurrency(), amount, investment.getNextReviewDate());
    }

    /**
     * Manually resets the market reference.
     * Stores old and new reference, date, reason into audit history table.
     */
    @Transactional
    public void resetReference(Long investmentId, BigDecimal newReferenceValue, LocalDate referenceDate, String reason) {
        Investment investment = investmentRepository.findById(investmentId)
                .orElseThrow(() -> new IllegalArgumentException("Investment not found with ID: " + investmentId));

        String refType = investment.getName().contains("ATLAS") ? "SP500" : "GOLD_ETF";

        MarketReference newReference = new MarketReference(investmentId, refType, newReferenceValue, referenceDate, reason);
        marketReferenceRepository.save(newReference);

        log.info("Market reference reset for '{}' to {} on {} (Reason: {})",
                investment.getName(), newReferenceValue, referenceDate, reason);
    }

    private Optional<MarketDataSnapshot> fetchMarketSnapshot(Investment investment) {
        if (investment.getName().toUpperCase().contains("ATLAS")) {
            return marketDataService.getSp500Level();
        } else {
            return marketDataService.getIciciGoldEtfPrice();
        }
    }

    private AppProperties.TierConfig getTierConfig(Investment investment) {
        if (investment.getName().toUpperCase().contains("ATLAS")) {
            return properties.getInvestments().getAtlas();
        } else {
            return properties.getInvestments().getGold();
        }
    }
}
