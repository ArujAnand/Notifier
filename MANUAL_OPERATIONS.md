# Manual Operations Guide 📋

This guide provides exact step-by-step instructions for operating Investment Sentinel, setting baseline references, recording contributions, and managing schedule reviews.

---

## 1. Setting Initial Market Reference Values

When you first launch the application, initial baseline reference values are seeded by Flyway migration `V2`:
- **ATLAS GLOBAL**: S&P 500 Baseline = `5800.00`
- **ICICI Prudential Gold ETF**: Gold ETF Baseline = `72.50`

### To Set Your Own Actual Baseline Reference:
1. Open the Web Dashboard (`http://localhost:8080` or your Render URL).
2. On the **ATLAS GLOBAL** card, click **"↺ Reset Reference"**.
3. Fill in:
   - **New Reference Value**: e.g., `5850.50` (the actual S&P 500 level on your starting date).
   - **Effective Date**: e.g., `2026-07-07`.
   - **Reason**: `Initial portfolio baseline starting reference`.
4. Click **Confirm Reset**.
5. The system records an immutable audit entry in `market_references` table and begins measuring future drawdowns against `5850.50`.
6. Repeat for **ICICI Prudential Gold ETF** by clicking its **"↺ Reset Reference"** button with your Gold reference price (e.g. `74.20`).

---

## 2. Recording Your First $50 ATLAS GLOBAL Investment

When you manually contribute money to ATLAS GLOBAL on smallcase:

1. On the dashboard, locate the **ATLAS GLOBAL** card.
2. Click **"+ Record Investment"**.
3. Enter:
   - **Contribution Amount**: `50.00`
   - **Investment Date**: `2026-10-07`
   - **Notes**: `Initial smallcase contribution at -6.9% drawdown`
4. Click **Save Contribution**.
5. What the system does automatically:
   - Sets `last_investment_amount = 50.00`
   - Sets `last_investment_date = 2026-10-07`
   - Calculates `next_review_date = 2027-01-07` (exactly 3 calendar months later!)
   - Stores an entry in `historical_investments` table for lifetime records.
   - **CRITICAL**: The S&P 500 baseline reference remains completely unchanged!

---

## 3. Recording Your First ICICI Prudential Gold ETF Investment

When you purchase units of ICICI Prudential Gold ETF:

1. On the dashboard, locate the **ICICI Prudential Gold ETF** card.
2. Click **"+ Record Investment"**.
3. Enter:
   - **Contribution Amount**: `2000.00` (or configured amount in INR)
   - **Investment Date**: `2026-10-07`
   - **Notes**: `Initial ETF allocation on NSE`
4. Click **Save Contribution**.
5. The system updates `last_investment_date`, calculates `next_review_date = 2027-01-07`, and preserves your baseline reference.

---

## 4. Resetting a Market Reference (When and Why)

### When should you Reset Reference?
- Only when you explicitly decide to establish a new cost basis or baseline after a major portfolio rebalancing or annual review.

### When should you NOT Reset Reference?
- **Never** automatically reset after a market recovery.
- **Never** reset automatically when recording an investment.

If S&P 500 recovers from 5,500 back to 6,200, the reference remains at 5,800 unless you manually click **"Reset Reference"**.

---

## 5. Triggering an On-Demand Daily Evaluation

To trigger the daily evaluation immediately (without waiting for the 08:00 AM IST cron job):
- Click **"▶ Run Daily Check Now"** in the top navigation bar.
- The system will fetch fresh quotes, check the 3-month calendar review dates, evaluate drawdown thresholds, send any pending SMS, and refresh the UI.
- Because the evaluation is **idempotent**, clicking it multiple times will never produce duplicate SMS alerts!
