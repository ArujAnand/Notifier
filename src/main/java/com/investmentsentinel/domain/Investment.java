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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "investments")
public class Investment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, length = 10)
    private String currency; // 'USD' or 'INR'

    @Column(name = "last_investment_date")
    private LocalDate lastInvestmentDate;

    @Column(name = "last_investment_amount", precision = 15, scale = 2)
    private BigDecimal lastInvestmentAmount;

    @Column(name = "next_review_date")
    private LocalDate nextReviewDate;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public Investment() {}

    public Investment(String name, String currency) {
        this.name = name;
        this.currency = currency;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    /**
     * Records a new investment and sets the next review date using strict calendar-month logic.
     * Does NOT reset market reference.
     */
    public void recordInvestment(BigDecimal amount, LocalDate date, int reviewIntervalMonths) {
        this.lastInvestmentAmount = amount;
        this.lastInvestmentDate = date;
        // Strict calendar-month addition (handles 28/29/30/31 days appropriately)
        this.nextReviewDate = date.plusMonths(reviewIntervalMonths);
    }

    /**
     * Determines whether the investment review is due as of the given date.
     */
    public boolean isReviewDue(LocalDate currentDate) {
        if (nextReviewDate == null) {
            return true;
        }
        return !currentDate.isBefore(nextReviewDate);
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public LocalDate getLastInvestmentDate() { return lastInvestmentDate; }
    public void setLastInvestmentDate(LocalDate lastInvestmentDate) { this.lastInvestmentDate = lastInvestmentDate; }

    public BigDecimal getLastInvestmentAmount() { return lastInvestmentAmount; }
    public void setLastInvestmentAmount(BigDecimal lastInvestmentAmount) { this.lastInvestmentAmount = lastInvestmentAmount; }

    public LocalDate getNextReviewDate() { return nextReviewDate; }
    public void setNextReviewDate(LocalDate nextReviewDate) { this.nextReviewDate = nextReviewDate; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
