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
@Table(name = "notifications")
public class NotificationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "investment_id")
    private Long investmentId;

    @Column(name = "notification_type", nullable = false, length = 50)
    private String notificationType;

    @Column(precision = 6, scale = 2)
    private BigDecimal threshold;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "sent_at", nullable = false, updatable = false)
    private OffsetDateTime sentAt;

    @Column(nullable = false, length = 50)
    private String provider;

    @Column(name = "provider_message_id", length = 100)
    private String providerMessageId;

    @Column(nullable = false, length = 20)
    private String status; // 'SENT', 'FAILED', 'PENDING'

    public NotificationRecord() {}

    public NotificationRecord(Long investmentId, String notificationType, BigDecimal threshold,
                              String message, String provider, String providerMessageId, String status) {
        this.investmentId = investmentId;
        this.notificationType = notificationType;
        this.threshold = threshold;
        this.message = message;
        this.provider = provider;
        this.providerMessageId = providerMessageId;
        this.status = status;
    }

    @PrePersist
    protected void onCreate() {
        this.sentAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getInvestmentId() { return investmentId; }
    public void setInvestmentId(Long investmentId) { this.investmentId = investmentId; }

    public String getNotificationType() { return notificationType; }
    public void setNotificationType(String notificationType) { this.notificationType = notificationType; }

    public BigDecimal getThreshold() { return threshold; }
    public void setThreshold(BigDecimal threshold) { this.threshold = threshold; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public OffsetDateTime getSentAt() { return sentAt; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getProviderMessageId() { return providerMessageId; }
    public void setProviderMessageId(String providerMessageId) { this.providerMessageId = providerMessageId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
