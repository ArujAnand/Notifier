package com.investmentsentinel.config;

import java.math.BigDecimal;
import java.time.ZoneId;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String timezone = "Asia/Kolkata";
    private Monitoring monitoring = new Monitoring();
    private Sms sms = new Sms();
    private MarketData marketData = new MarketData();
    private Investments investments = new Investments();

    public ZoneId getZoneId() {
        return ZoneId.of(timezone);
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public Monitoring getMonitoring() {
        return monitoring;
    }

    public void setMonitoring(Monitoring monitoring) {
        this.monitoring = monitoring;
    }

    public Sms getSms() {
        return sms;
    }

    public void setSms(Sms sms) {
        this.sms = sms;
    }

    public MarketData getMarketData() {
        return marketData;
    }

    public void setMarketData(MarketData marketData) {
        this.marketData = marketData;
    }

    public Investments getInvestments() {
        return investments;
    }

    public void setInvestments(Investments investments) {
        this.investments = investments;
    }

    public static class Monitoring {
        private String cron = "0 0 8 * * ?";
        private boolean earlyDrawdownAlerts = true;
        private int reviewIntervalMonths = 3;

        public String getCron() {
            return cron;
        }

        public void setCron(String cron) {
            this.cron = cron;
        }

        public boolean isEarlyDrawdownAlerts() {
            return earlyDrawdownAlerts;
        }

        public void setEarlyDrawdownAlerts(boolean earlyDrawdownAlerts) {
            this.earlyDrawdownAlerts = earlyDrawdownAlerts;
        }

        public int getReviewIntervalMonths() {
            return reviewIntervalMonths;
        }

        public void setReviewIntervalMonths(int reviewIntervalMonths) {
            this.reviewIntervalMonths = reviewIntervalMonths;
        }
    }

    public static class Sms {
        private String provider = "mock";
        private String apiKey = "";
        private String senderId = "SENTINEL";
        private String recipient = "+919876543210";
        private String dltTemplateId = "";
        private int maxRetries = 3;
        private long retryDelayMs = 2000;

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getSenderId() {
            return senderId;
        }

        public void setSenderId(String senderId) {
            this.senderId = senderId;
        }

        public String getRecipient() {
            return recipient;
        }

        public void setRecipient(String recipient) {
            this.recipient = recipient;
        }

        public String getDltTemplateId() {
            return dltTemplateId;
        }

        public void setDltTemplateId(String dltTemplateId) {
            this.dltTemplateId = dltTemplateId;
        }

        public int getMaxRetries() {
            return maxRetries;
        }

        public void setMaxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
        }

        public long getRetryDelayMs() {
            return retryDelayMs;
        }

        public void setRetryDelayMs(long retryDelayMs) {
            this.retryDelayMs = retryDelayMs;
        }
    }

    public static class MarketData {
        private String provider = "yahoo";
        private String alphaVantageKey = "";
        private int staleThresholdHours = 72;

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public String getAlphaVantageKey() {
            return alphaVantageKey;
        }

        public void setAlphaVantageKey(String alphaVantageKey) {
            this.alphaVantageKey = alphaVantageKey;
        }

        public int getStaleThresholdHours() {
            return staleThresholdHours;
        }

        public void setStaleThresholdHours(int staleThresholdHours) {
            this.staleThresholdHours = staleThresholdHours;
        }
    }

    public static class Investments {
        private TierConfig atlas = new TierConfig(
                "USD",
                new BigDecimal("25.0"),
                new BigDecimal("5.0"), new BigDecimal("50.0"),
                new BigDecimal("15.0"), new BigDecimal("75.0"),
                new BigDecimal("25.0"), new BigDecimal("100.0")
        );
        private TierConfig gold = new TierConfig(
                "INR",
                new BigDecimal("2000.0"),
                new BigDecimal("5.0"), new BigDecimal("4000.0"),
                new BigDecimal("15.0"), new BigDecimal("6000.0"),
                new BigDecimal("25.0"), new BigDecimal("8000.0")
        );

        public TierConfig getAtlas() {
            return atlas;
        }

        public void setAtlas(TierConfig atlas) {
            this.atlas = atlas;
        }

        public TierConfig getGold() {
            return gold;
        }

        public void setGold(TierConfig gold) {
            this.gold = gold;
        }
    }

    public static class TierConfig {
        private String currency;
        private BigDecimal normalAmount;
        private BigDecimal drawdown1;
        private BigDecimal amount1;
        private BigDecimal drawdown2;
        private BigDecimal amount2;
        private BigDecimal drawdown3;
        private BigDecimal amount3;

        public TierConfig() {}

        public TierConfig(String currency, BigDecimal normalAmount, BigDecimal drawdown1, BigDecimal amount1,
                          BigDecimal drawdown2, BigDecimal amount2, BigDecimal drawdown3, BigDecimal amount3) {
            this.currency = currency;
            this.normalAmount = normalAmount;
            this.drawdown1 = drawdown1;
            this.amount1 = amount1;
            this.drawdown2 = drawdown2;
            this.amount2 = amount2;
            this.drawdown3 = drawdown3;
            this.amount3 = amount3;
        }

        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }

        public BigDecimal getNormalAmount() { return normalAmount; }
        public void setNormalAmount(BigDecimal normalAmount) { this.normalAmount = normalAmount; }

        public BigDecimal getDrawdown1() { return drawdown1; }
        public void setDrawdown1(BigDecimal drawdown1) { this.drawdown1 = drawdown1; }

        public BigDecimal getAmount1() { return amount1; }
        public void setAmount1(BigDecimal amount1) { this.amount1 = amount1; }

        public BigDecimal getDrawdown2() { return drawdown2; }
        public void setDrawdown2(BigDecimal drawdown2) { this.drawdown2 = drawdown2; }

        public BigDecimal getAmount2() { return amount2; }
        public void setAmount2(BigDecimal amount2) { this.amount2 = amount2; }

        public BigDecimal getDrawdown3() { return drawdown3; }
        public void setDrawdown3(BigDecimal drawdown3) { this.drawdown3 = drawdown3; }

        public BigDecimal getAmount3() { return amount3; }
        public void setAmount3(BigDecimal amount3) { this.amount3 = amount3; }
    }
}
