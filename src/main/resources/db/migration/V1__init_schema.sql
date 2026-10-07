-- V1__init_schema.sql
-- Personal Investment Sentinel Schema

CREATE TABLE IF NOT EXISTS investments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    currency VARCHAR(10) NOT NULL,
    last_investment_date DATE,
    last_investment_amount NUMERIC(15, 2),
    next_review_date DATE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS market_references (
    id BIGSERIAL PRIMARY KEY,
    investment_id BIGINT NOT NULL REFERENCES investments(id) ON DELETE CASCADE,
    reference_type VARCHAR(50) NOT NULL,
    reference_value NUMERIC(15, 4) NOT NULL,
    reference_date DATE NOT NULL,
    reason VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_market_references_invest_date ON market_references(investment_id, reference_date DESC);

CREATE TABLE IF NOT EXISTS market_snapshots (
    id BIGSERIAL PRIMARY KEY,
    asset VARCHAR(50) NOT NULL,
    value NUMERIC(15, 4) NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    data_provider VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_market_snapshots_asset_time ON market_snapshots(asset, timestamp DESC);

CREATE TABLE IF NOT EXISTS trigger_events (
    id BIGSERIAL PRIMARY KEY,
    investment_id BIGINT NOT NULL REFERENCES investments(id) ON DELETE CASCADE,
    threshold NUMERIC(6, 2) NOT NULL,
    drawdown NUMERIC(6, 2) NOT NULL,
    market_value NUMERIC(15, 4) NOT NULL,
    reference_value NUMERIC(15, 4) NOT NULL,
    trigger_type VARCHAR(50) NOT NULL, -- 'THRESHOLD_ALERT', 'SCHEDULED_REVIEW', 'THRESHOLD_RECOVERY'
    triggered_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_trigger_events_invest_time ON trigger_events(investment_id, triggered_at DESC);

CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    investment_id BIGINT REFERENCES investments(id) ON DELETE SET NULL,
    notification_type VARCHAR(50) NOT NULL, -- 'SCHEDULED_REVIEW', 'THRESHOLD_ALERT', 'SYSTEM_TEST'
    threshold NUMERIC(6, 2),
    message TEXT NOT NULL,
    sent_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    provider VARCHAR(50) NOT NULL,
    provider_message_id VARCHAR(100),
    status VARCHAR(20) NOT NULL -- 'SENT', 'FAILED', 'PENDING'
);

CREATE INDEX IF NOT EXISTS idx_notifications_invest_time ON notifications(investment_id, sent_at DESC);

CREATE TABLE IF NOT EXISTS historical_investments (
    id BIGSERIAL PRIMARY KEY,
    investment_id BIGINT NOT NULL REFERENCES investments(id) ON DELETE CASCADE,
    amount NUMERIC(15, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    investment_date DATE NOT NULL,
    notes VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_historical_investments_invest_date ON historical_investments(investment_id, investment_date DESC);
