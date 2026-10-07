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
@Table(name = "market_snapshots")
public class MarketSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String asset; // e.g. 'SP500', 'GOLD_ETF'

    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal value;

    @Column(nullable = false)
    private OffsetDateTime timestamp;

    @Column(name = "data_provider", nullable = false, length = 50)
    private String dataProvider;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public MarketSnapshot() {}

    public MarketSnapshot(String asset, BigDecimal value, OffsetDateTime timestamp, String dataProvider) {
        this.asset = asset;
        this.value = value;
        this.timestamp = timestamp;
        this.dataProvider = dataProvider;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAsset() { return asset; }
    public void setAsset(String asset) { this.asset = asset; }

    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }

    public OffsetDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(OffsetDateTime timestamp) { this.timestamp = timestamp; }

    public String getDataProvider() { return dataProvider; }
    public void setDataProvider(String dataProvider) { this.dataProvider = dataProvider; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
}
