package com.investmentsentinel.service.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.investmentsentinel.service.monitoring.InvestmentMonitoringService;

/**
 * Scheduled job executing once per day at configured time (default 08:00 AM IST).
 * Uses ZoneId.of("Asia/Kolkata").
 * Idempotent execution: duplicate runs on the same day never dispatch repeated notifications.
 */
@Component
public class DailyMonitoringJob {

    private static final Logger log = LoggerFactory.getLogger(DailyMonitoringJob.class);

    private final InvestmentMonitoringService monitoringService;

    public DailyMonitoringJob(InvestmentMonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    @Scheduled(cron = "${app.monitoring.cron:0 0 8 * * ?}", zone = "${app.timezone:Asia/Kolkata}")
    public void runDailyEvaluationSchedule() {
        log.info("⏰ Triggering scheduled daily investment monitoring cycle...");
        try {
            monitoringService.runDailyEvaluation();
        } catch (Exception e) {
            log.error("Unhandled exception during daily investment monitoring job: {}", e.getMessage(), e);
        }
    }
}
