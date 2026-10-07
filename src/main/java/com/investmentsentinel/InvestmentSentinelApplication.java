package com.investmentsentinel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.investmentsentinel.config.AppProperties;

/**
 * Personal Investment Monitoring and SMS Notification System.
 * Purely monitors configured drawdown triggers and calendar review dates.
 * Strictly does NOT execute trades or connect to brokerage accounts.
 */
@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(AppProperties.class)
public class InvestmentSentinelApplication {

    public static void main(String[] args) {
        SpringApplication.run(InvestmentSentinelApplication.class, args);
    }
}
