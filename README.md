# Investment Sentinel 🛡️
### Personal Drawdown Monitoring & SMS Alert System

> **CRITICAL INVESTMENT SAFETY RULE**  
> This system is **NOT** an investment advisor and must **NOT** pretend to predict the market.  
> It does not say "The market will crash." Instead, it reports objective facts: *"The S&P 500 is X% below your configured reference."*  
> It will **NEVER** automatically place trades, buy assets, or connect to brokerage APIs. It is strictly a single-user monitoring and SMS reminder tool.

---

## 1. Overview & Architecture

Investment Sentinel is a streamlined, single-user Java 21 & Spring Boot 3 service designed to monitor two specific investments:
1. **ATLAS GLOBAL** (US/Global equity smallcase): Reference index is S&P 500 (`^GSPC`).
2. **ICICI Prudential Gold ETF** (Indian Gold ETF): Reference index is ICICI Prudential Gold ETF (`ICICIGOLD.NS`) or Gold benchmark.

### Core Principles
- **Calendar-Month Logic**: Evaluates calendar intervals (e.g. 10 January + 3 months = 10 April; Jan 31 + 3 months = Apr 30), not simple 90-day approximations.
- **Reference Preservation**: Recording an investment updates review dates but **never** resets your baseline reference level. Reference levels are only updated through explicit manual reset with full audit logging.
- **Smart SMS Deduplication**: Never sends daily repeat SMS when the market remains at the same drawdown level. Alerts trigger only when a *new* threshold is crossed (-5%, -15%, -25%) or when the market recovers above a threshold and later recrosses.
- **100% Free Hosting Ready**: Optimized for zero-cost deployment on Render, Koyeb, or Fly.io with persistent PostgreSQL (Neon / Supabase).

---

## 2. Technology Stack

- **Runtime**: Java 21 LTS (Eclipse Temurin)
- **Framework**: Spring Boot 3.3.4
- **Database**: PostgreSQL 16+ (persistent remote storage, no ephemeral local filesystem dependency)
- **Migrations**: Flyway (`db/migration/V1__init_schema.sql`, `V2__seed_initial_investments.sql`)
- **HTTP Client**: Spring 3 `RestClient` and `WebClient` with connection timeouts and bounded retry
- **Scheduler**: Spring `@Scheduled` (`0 0 8 * * ?` at `ZoneId.of("Asia/Kolkata")`)
- **Containerization**: Multi-stage `Dockerfile` with non-root user and memory limits (`-XX:MaxRAMPercentage=75.0`)
- **SMS Gateway**: Pluggable `SmsService` with implementations for Indian SMS providers (**StartMessaging**, **TechTo Networks**, **Fast2SMS**, and **LoggingMockSmsClient**).

---

## 3. Quickstart: Local Development with Docker Compose

### Prerequisites
- Docker and Docker Compose
- Java 21 & Maven (optional for containerized runs)

### Steps
1. **Clone repository and configure environment:**
   ```bash
   cp .env.example .env
   ```
   *(By default, `SMS_PROVIDER=mock` and `MARKET_DATA_PROVIDER=yahoo`, requiring zero external API keys to start!)*

2. **Launch with Docker Compose:**
   ```bash
   docker-compose up --build
   ```

3. **Access the Web Dashboard:**
   Open your browser to:
   [http://localhost:8080](http://localhost:8080)

4. **Verify Application Health:**
   ```bash
   curl http://localhost:8080/actuator/health
   # Returns: {"status":"UP"}
   ```

---

## 4. Building & Testing Locally with Maven

```bash
# Run all unit tests (includes calendar month, leap year, boundary tests -4.99% vs -5.00%, SMS retries)
mvn clean test

# Package standalone production JAR
mvn clean package -DskipTests

# Run JAR directly (pointing to local or remote PostgreSQL)
export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/investment_sentinel"
export SPRING_DATASOURCE_USERNAME="sentinel_user"
export SPRING_DATASOURCE_PASSWORD="sentinel_secret_password"
java -jar target/investment-sentinel-1.0.0.jar
```

---

## 5. Contribution Rules & Configurable Tiers

### ATLAS GLOBAL (Currency: USD)
| Condition | S&P 500 Drawdown | Suggested Contribution |
| :--- | :--- | :--- |
| **Normal** | Drawdown > -5.00% | **$25** |
| **Tier 1** | Drawdown ≤ -5.00% | **$50** |
| **Tier 2** | Drawdown ≤ -15.00% | **$75** |
| **Tier 3** | Drawdown ≤ -25.00% | **$100** |

### ICICI Prudential Gold ETF (Currency: INR)
| Condition | Gold Drawdown | Suggested Contribution |
| :--- | :--- | :--- |
| **Normal** | Drawdown > -5.00% | **₹2,000** |
| **Tier 1** | Drawdown ≤ -5.00% | **₹4,000** |
| **Tier 2** | Drawdown ≤ -15.00% | **₹6,000** |
| **Tier 3** | Drawdown ≤ -25.00% | **₹8,000** |

All amounts and thresholds are configurable via `.env` without modifying code.

---

## 6. Daily Scheduling & Idempotency

- Every day at **08:00 AM IST** (`Asia/Kolkata`), the scheduler runs:
  1. Fetches current market quotes (`^GSPC` for S&P 500 and `ICICIGOLD.NS` for Gold ETF).
  2. Verifies timestamp freshness (rejects data older than 72 hours to prevent false triggers during long holiday weekends).
  3. Computes drawdowns against baseline reference values.
  4. Checks whether the 3-month calendar review date has arrived.
  5. Evaluates threshold crossings (-5%, -15%, -25%).
  6. Dispatches SMS **only if required** (avoiding repeats).
  7. Audits triggers and notification statuses into PostgreSQL.
- Running the job twice on the same day is completely idempotent.

---

## 7. Additional Guides

- **[DEPLOYMENT.md](DEPLOYMENT.md)**: Deploy on Render or Koyeb free tiers with free PostgreSQL.
- **[SMS_PROVIDERS.md](SMS_PROVIDERS.md)**: Free trial credits, StartMessaging, TechTo, Fast2SMS, and Indian DLT compliance.
- **[MANUAL_OPERATIONS.md](MANUAL_OPERATIONS.md)**: Setting initial references, recording first contributions, and resetting references.
