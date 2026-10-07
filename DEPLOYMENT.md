# Free-Tier Deployment Guide 🚀

This document explains how to deploy **Investment Sentinel** on genuine, zero-cost cloud platforms with **no local filesystem dependence** and **no paid domain required**.

---

## Architecture for Free-Tier Deployment

```
   ┌──────────────────────────────────────────────┐
   │             Render.com / Koyeb               │
   │  Docker Web Service (Spring Boot 3 / Java 21)│
   │  - Free Tier (512MB RAM, Auto-sleep/Resume)  │
   │  - Configured with Asia/Kolkata Timezone     │
   └───────────────────────▲──────────────────────┘
                           │ JDBC over TLS
   ┌───────────────────────▼──────────────────────┐
   │         Neon.tech / Supabase / Render        │
   │           PostgreSQL (Persistent Free)       │
   │  - Never loses data on container redeploy    │
   │  - Automatic Flyway migrations V1 & V2       │
   └──────────────────────────────────────────────┘
```

---

## Step 1: Provision a Free Persistent PostgreSQL Database

Because free container hosting can sleep or restart, persistent data **must** live in an external database.

### Recommended Provider: **Neon (neon.tech)**
1. Create a free account at [https://neon.tech](https://neon.tech) (Free tier includes 0.5 GB storage, always free).
2. Create a new project named `investment-sentinel`.
3. In the Neon dashboard, copy your **Pooled Connection String**:
   ```
   postgresql://sentinel_user:AbCdEf123456@ep-cool-fog-123456.us-east-2.aws.neon.tech/investment_sentinel?sslmode=require
   ```
4. Convert to Spring JDBC URL format:
   ```
   SPRING_DATASOURCE_URL=jdbc:postgresql://ep-cool-fog-123456.us-east-2.aws.neon.tech/investment_sentinel?sslmode=require
   SPRING_DATASOURCE_USERNAME=sentinel_user
   SPRING_DATASOURCE_PASSWORD=AbCdEf123456
   ```

*Alternative free PostgreSQL providers: Supabase (supabase.com) or Render PostgreSQL Free.*

---

## Step 2: Deploy to Render Free Tier (Option 1 - Preferred)

1. **Push your code to GitHub** (private or public repository).
2. Log in to [https://render.com](https://render.com).
3. Click **New +** > **Web Service**.
4. Connect your GitHub repository.
5. Configure the deployment settings:
   - **Name**: `investment-sentinel`
   - **Region**: Singapore or Frankfurt (close to India)
   - **Environment**: `Docker` (Render automatically detects your `Dockerfile`)
   - **Instance Type**: `Free` (0.1 CPU, 512 MB RAM)
6. Add the following **Environment Variables** in the Render dashboard:

| Variable Name | Example / Value | Description |
| :--- | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | `prod` | Activates production profile |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://ep-...aws.neon.tech/investment_sentinel?sslmode=require` | Neon JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | `sentinel_user` | Database user |
| `SPRING_DATASOURCE_PASSWORD` | `your_secret_db_password` | Database password |
| `SMS_PROVIDER` | `fast2sms` *(or `startmessaging`, `mock`)* | Selected SMS gateway |
| `SMS_API_KEY` | `your_sms_provider_api_key` | Vendor API token |
| `SMS_RECIPIENT` | `+919876543210` | Your verified mobile number |
| `SMS_SENDER_ID` | `SENTINEL` | 6-character sender ID |
| `TIMEZONE` | `Asia/Kolkata` | Daily scheduling timezone |
| `EARLY_DRAWDOWN_ALERTS` | `true` | Send alerts on threshold crosses |
| `JAVA_OPTS` | `-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC` | Keeps JVM within 512MB RAM |

7. Click **Create Web Service**.
8. Render will build the Docker container and start your service at:
   `https://investment-sentinel.onrender.com`

---

## Step 3: Deploy to Koyeb Free Tier (Option 2 - Alternative)

1. Log in to [https://app.koyeb.com](https://app.koyeb.com).
2. Click **Create Service**.
3. Select **GitHub** and pick your repository.
4. Select **Dockerfile** as the build method.
5. In **Instance Type**, select `Nano` (Free tier, 512MB RAM).
6. Under **Environment variables**, paste the same environment variables as listed above in Step 2.
7. Click **Deploy**.
8. Your service will be live on a secure HTTPS `*.koyeb.app` URL.

---

## Step 4: Verification & Initial Run

1. Open your assigned URL (e.g. `https://investment-sentinel.onrender.com`).
2. You will see the server-rendered dashboard showing **ATLAS GLOBAL** and **ICICI Prudential Gold ETF**.
3. Click **"▶ Run Daily Check Now"** to run an immediate on-demand evaluation.
4. Check the application logs in Render/Koyeb dashboard to confirm market data was fetched and Flyway migrations ran cleanly.
5. Click **"Send Test SMS"** to verify that SMS delivery works to your mobile number.
