package com.investmentsentinel.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "market_references")
public class MarketReference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "investment_id", nullable = false)
    private Long investmentId;

    @Column(name = "reference_type", nullable = false, length = 50)
    private String referenceType; // e.g. 'SP500', 'GOLD_ETF'

    @Column(name = "reference_value", nullable = false, precision = 15, scale = 4)
    private BigDecimal referenceValue;

    @Column(name = "reference_date", nullable = false)
    private LocalDate referenceDate;

    @Column(nullable = false, length = 255)
    private String reason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public MarketReference() {}

    public MarketReference(Long investmentId, String referenceType, BigDecimal referenceValue, LocalDate referenceDate, String reason) {
        this.investmentId = investmentId;
        this.referenceType = referenceType;
        this.referenceValue = referenceValue;
        this.referenceDate = referenceDate;
        this.reason = reason;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getInvestmentId() { return investmentId; }
    public void setInvestmentId(Long investmentId) { this.investmentId = investmentId; }

    public String getReferenceType() { return referenceType; }
    public void setReferenceType(String referenceType) { this.referenceType = referenceType; }

    public BigDecimal getReferenceValue() { return referenceValue; }
    public void setReferenceValue(BigDecimal referenceValue) { this.referenceValue = referenceValue; }

    public LocalDate getReferenceDate() { return referenceDate; }
    public void setReferenceDate(LocalDate referenceDate) { this.referenceDate = referenceDate; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
}
