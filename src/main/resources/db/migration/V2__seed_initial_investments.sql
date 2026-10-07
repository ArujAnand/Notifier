-- V2__seed_initial_investments.sql
-- Initial seed for ATLAS GLOBAL and ICICI Prudential Gold ETF

INSERT INTO investments (id, name, currency, last_investment_date, last_investment_amount, next_review_date, active)
VALUES
    (1, 'ATLAS GLOBAL', 'USD', '2026-07-07', 50.00, '2026-10-07', TRUE),
    (2, 'ICICI Prudential Gold ETF', 'INR', '2026-07-07', 2000.00, '2026-10-07', TRUE)
ON CONFLICT (id) DO NOTHING;

-- Initial reference levels
INSERT INTO market_references (investment_id, reference_type, reference_value, reference_date, reason)
VALUES
    (1, 'SP500', 5800.0000, '2026-07-07', 'Initial portfolio baseline'),
    (2, 'GOLD_ETF', 72.5000, '2026-07-07', 'Initial portfolio baseline')
ON CONFLICT DO NOTHING;

-- Record initial historical investments
INSERT INTO historical_investments (investment_id, amount, currency, investment_date, notes)
VALUES
    (1, 50.00, 'USD', '2026-07-07', 'Initial recorded contribution'),
    (2, 2000.00, 'INR', '2026-07-07', 'Initial recorded contribution')
ON CONFLICT DO NOTHING;

-- Reset sequence to avoid collision on next inserts
SELECT setval('investments_id_seq', (SELECT MAX(id) FROM investments));
