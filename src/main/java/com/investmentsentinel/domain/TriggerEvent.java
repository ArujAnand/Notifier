package com.investmentsentinel.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "trigger_events")
public class TriggerEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "investment_id", nullable = false)
    private Long investmentId;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal threshold;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal drawdown;

    @Column(name = "market_value", nullable = false, precision = 15, scale = 4)
    private BigDecimal marketValue;

    @Column(name = "reference_value", nullable = false, precision = 15, scale = 4)
    private BigDecimal referenceValue;

    @Column(name = "trigger_type", nullable = false, length = 50)
    private String triggerType;

    @Column(name = "triggered_at", nullable = false, updatable = false)
    private OffsetDateTime triggeredAt;

    public TriggerEvent() {}

    public TriggerEvent(Long investmentId, BigDecimal threshold, BigDecimal drawdown,
                        BigDecimal marketValue, BigDecimal referenceValue, String triggerType) {
        this.investmentId = investmentId;
        this.threshold = threshold;
        this.drawdown = drawdown;
        this.marketValue = marketValue;
        this.referenceValue = referenceValue;
        this.triggerType = triggerType;
    }

    @PrePersist
    protected void onCreate() {
        this.triggeredAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getInvestmentId() { return investmentId; }
    public void setInvestmentId(Long investmentId) { this.investmentId = investmentId; }

    public BigDecimal getThreshold() { return threshold; }
    public void setThreshold(BigDecimal threshold) { this.threshold = threshold; }

    public BigDecimal getDrawdown() { return drawdown; }
    public void setDrawdown(BigDecimal drawdown) { this.drawdown = drawdown; }

    public BigDecimal getMarketValue() { return marketValue; }
    public void setMarketValue(BigDecimal marketValue) { this.marketValue = marketValue; }

    public BigDecimal getReferenceValue() { return referenceValue; }
    public void setReferenceValue(BigDecimal referenceValue) { this.referenceValue = referenceValue; }

    public String getTriggerType() { return triggerType; }
    public void setTriggerType(String triggerType) { this.triggerType = triggerType; }

    public OffsetDateTime getTriggeredAt() { return triggeredAt; }
}
