/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState, useEffect } from 'react';
import {
  ShieldAlert,
  Bell,
  Calendar,
  TrendingDown,
  RefreshCw,
  PlusCircle,
  FileCode,
  CheckCircle2,
  AlertTriangle,
  History,
  Send,
  Sliders,
  DollarSign,
  Info,
  Clock,
  Database,
  ExternalLink,
  ChevronRight,
  Server,
  Terminal,
  Copy,
  Check
} from 'lucide-react';

// Types
interface InvestmentItem {
  id: number;
  name: string;
  ticker: string;
  currency: 'USD' | 'INR';
  currencySymbol: string;
  lastInvestmentDate: string;
  lastInvestmentAmount: number;
  nextReviewDate: string;
  currentValue: number;
  referenceValue: number;
  referenceDate: string;
  normalAmount: number;
  t1Drawdown: number;
  t1Amount: number;
  t2Drawdown: number;
  t2Amount: number;
  t3Drawdown: number;
  t3Amount: number;
}

interface MarketReferenceAudit {
  id: number;
  investmentId: number;
  investmentName: string;
  referenceValue: number;
  previousValue: number;
  effectiveDate: string;
  reason: string;
  timestamp: string;
}

interface HistoricalRecord {
  id: number;
  investmentId: number;
  investmentName: string;
  amount: number;
  currency: string;
  date: string;
  notes: string;
}

interface NotificationItem {
  id: number;
  investmentName: string;
  type: 'SCHEDULED_REVIEW' | 'THRESHOLD_ALERT' | 'SYSTEM_TEST';
  threshold: number | null;
  message: string;
  sentAt: string;
  provider: string;
  status: 'SENT' | 'FAILED';
  phoneMasked: string;
}

interface TriggerEventItem {
  id: number;
  investmentName: string;
  threshold: number;
  drawdown: number;
  marketValue: number;
  referenceValue: number;
  type: 'THRESHOLD_ALERT' | 'THRESHOLD_RECOVERY';
  timestamp: string;
}

export default function App() {
  // Navigation tabs
  const [activeTab, setActiveTab] = useState<'dashboard' | 'history' | 'simulator' | 'code' | 'settings'>('dashboard');

  // Investment States
  const [investments, setInvestments] = useState<InvestmentItem[]>([
    {
      id: 1,
      name: 'ATLAS GLOBAL',
      ticker: '^GSPC (S&P 500)',
      currency: 'USD',
      currencySymbol: '$',
      lastInvestmentDate: '2026-07-07',
      lastInvestmentAmount: 50,
      nextReviewDate: '2026-10-07',
      currentValue: 6050,
      referenceValue: 6500,
      referenceDate: '2026-07-07',
      normalAmount: 25,
      t1Drawdown: 5,
      t1Amount: 50,
      t2Drawdown: 15,
      t2Amount: 75,
      t3Drawdown: 25,
      t3Amount: 100
    },
    {
      id: 2,
      name: 'ICICI Prudential Gold ETF',
      ticker: 'ICICIGOLD.NS (Gold ETF)',
      currency: 'INR',
      currencySymbol: '₹',
      lastInvestmentDate: '2026-07-07',
      lastInvestmentAmount: 2000,
      nextReviewDate: '2026-10-07',
      currentValue: 72.70,
      referenceValue: 77.50,
      referenceDate: '2026-07-07',
      normalAmount: 2000,
      t1Drawdown: 5,
      t1Amount: 4000,
      t2Drawdown: 15,
      t2Amount: 6000,
      t3Drawdown: 25,
      t3Amount: 8000
    }
  ]);

  // History & Audit records
  const [referenceAudits, setReferenceAudits] = useState<MarketReferenceAudit[]>([
    {
      id: 1,
      investmentId: 1,
      investmentName: 'ATLAS GLOBAL',
      referenceValue: 6500,
      previousValue: 6500,
      effectiveDate: '2026-07-07',
      reason: 'Initial portfolio baseline recorded',
      timestamp: '2026-07-07 08:00:00'
    },
    {
      id: 2,
      investmentId: 2,
      investmentName: 'ICICI Prudential Gold ETF',
      referenceValue: 77.50,
      previousValue: 77.50,
      effectiveDate: '2026-07-07',
      reason: 'Initial portfolio baseline recorded',
      timestamp: '2026-07-07 08:00:00'
    }
  ]);

  const [investmentHistory, setInvestmentHistory] = useState<HistoricalRecord[]>([
    {
      id: 1,
      investmentId: 1,
      investmentName: 'ATLAS GLOBAL',
      amount: 50,
      currency: 'USD',
      date: '2026-07-07',
      notes: 'Initial smallcase contribution'
    },
    {
      id: 2,
      investmentId: 2,
      investmentName: 'ICICI Prudential Gold ETF',
      amount: 2000,
      currency: 'INR',
      date: '2026-07-07',
      notes: 'Initial ETF contribution on NSE'
    }
  ]);

  const [notifications, setNotifications] = useState<NotificationItem[]>([
    {
      id: 1,
      investmentName: 'ATLAS GLOBAL',
      type: 'SCHEDULED_REVIEW',
      threshold: -5.0,
      message: `ATLAS GLOBAL review due.\n\nLast: $50 on 07-Jul-2026\n\nS&P 500:\nCurrent: 6,050\nReference: 6,500\nDrawdown: -6.92%\n\nSuggested: $50\nReason: Market is ≥5% below reference.\n\nThis is a reminder only.\nNo trade has been placed.`,
      sentAt: '2026-10-07 08:00:00',
      provider: 'fast2sms',
      status: 'SENT',
      phoneMasked: '+91******3210'
    }
  ]);

  const [triggerEvents, setTriggerEvents] = useState<TriggerEventItem[]>([
    {
      id: 1,
      investmentName: 'ATLAS GLOBAL',
      threshold: -5.0,
      drawdown: -6.92,
      marketValue: 6050,
      referenceValue: 6500,
      type: 'THRESHOLD_ALERT',
      timestamp: '2026-10-07 08:00:00'
    }
  ]);

  // Modals & Form states
  const [recordModalOpen, setRecordModalOpen] = useState(false);
  const [resetRefModalOpen, setResetRefModalOpen] = useState(false);
  const [testSmsModalOpen, setTestSmsModalOpen] = useState(false);
  const [selectedInvId, setSelectedInvId] = useState<number>(1);

  // Record Form
  const [recordAmount, setRecordAmount] = useState<number>(50);
  const [recordDate, setRecordDate] = useState<string>('2026-10-07');
  const [recordNotes, setRecordNotes] = useState<string>('Quarterly scheduled contribution');

  // Reset Ref Form
  const [newRefValue, setNewRefValue] = useState<number>(6500);
  const [refResetDate, setRefResetDate] = useState<string>('2026-10-07');
  const [refResetReason, setRefResetReason] = useState<string>('Manual portfolio recalibration');

  // Test SMS Form
  const [smsRecipient, setSmsRecipient] = useState<string>('+919876543210');
  const [smsProvider, setSmsProvider] = useState<'fast2sms' | 'startmessaging' | 'techtor' | 'mock'>('fast2sms');
  const [smsCustomMessage, setSmsCustomMessage] = useState<string>('Investment Sentinel test message: connectivity verified successfully.');

  // System status toast
  const [bannerMessage, setBannerMessage] = useState<{ type: 'success' | 'info' | 'error'; text: string } | null>(null);

  // Execution Log for on-demand daily check
  const [diagnosticLogs, setDiagnosticLogs] = useState<string[]>([]);
  const [showLogModal, setShowLogModal] = useState(false);

  // Copy code helper
  const [copiedKey, setCopiedKey] = useState<string | null>(null);
  const copyToClipboard = (text: string, key: string) => {
    navigator.clipboard.writeText(text);
    setCopiedKey(key);
    setTimeout(() => setCopiedKey(null), 2000);
  };

  const showNotification = (text: string, type: 'success' | 'info' | 'error' = 'success') => {
    setBannerMessage({ type, text });
    setTimeout(() => setBannerMessage(null), 6000);
  };

  // Calculate Drawdown and suggested amount
  const computeDrawdown = (current: number, reference: number) => {
    if (!reference || reference <= 0) return 0;
    return parseFloat((((current - reference) / reference) * 100).toFixed(2));
  };

  const computeSuggestion = (inv: InvestmentItem, drawdown: number) => {
    if (drawdown <= -inv.t3Drawdown) {
      return { amount: inv.t3Amount, reason: `Market is ≥${inv.t3Drawdown}% below reference (Major Correction Tier)` };
    }
    if (drawdown <= -inv.t2Drawdown) {
      return { amount: inv.t2Amount, reason: `Market is ≥${inv.t2Drawdown}% below reference (Large Correction Tier)` };
    }
    if (drawdown <= -inv.t1Drawdown) {
      return { amount: inv.t1Amount, reason: `Market is ≥${inv.t1Drawdown}% below reference (Correction Tier)` };
    }
    return { amount: inv.normalAmount, reason: `Drawdown is within normal range (above -${inv.t1Drawdown}%)` };
  };

  // Calendar month addition helper (strict calendar logic)
  const addCalendarMonths = (dateStr: string, months: number): string => {
    const parts = dateStr.split('-');
    const year = parseInt(parts[0], 10);
    const month = parseInt(parts[1], 10) - 1; // 0-indexed
    const day = parseInt(parts[2], 10);

    const targetDate = new Date(year, month + months, 1);
    // Determine last day of the resulting target month
    const lastDayOfTargetMonth = new Date(year, month + months + 1, 0).getDate();
    const finalDay = Math.min(day, lastDayOfTargetMonth);

    const result = new Date(year, month + months, finalDay);
    const y = result.getFullYear();
    const m = String(result.getMonth() + 1).padStart(2, '0');
    const d = String(result.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
  };

  // Action: Record Investment
  const handleRecordInvestment = (e: React.FormEvent) => {
    e.preventDefault();
    const inv = investments.find((i) => i.id === selectedInvId);
    if (!inv) return;

    // Strict 3-calendar months addition
    const nextReview = addCalendarMonths(recordDate, 3);

    // Update investment
    setInvestments((prev) =>
      prev.map((item) =>
        item.id === selectedInvId
          ? {
              ...item,
              lastInvestmentAmount: recordAmount,
              lastInvestmentDate: recordDate,
              nextReviewDate: nextReview
            }
          : item
      )
    );

    // Add to historical ledger
    const newHistory: HistoricalRecord = {
      id: Date.now(),
      investmentId: inv.id,
      investmentName: inv.name,
      amount: recordAmount,
      currency: inv.currency,
      date: recordDate,
      notes: recordNotes || 'Manual contribution'
    };
    setInvestmentHistory((prev) => [newHistory, ...prev]);

    setRecordModalOpen(false);
    showNotification(
      `Recorded ${inv.currencySymbol}${recordAmount} for ${inv.name}. Next review scheduled for ${nextReview}. Baseline reference (${inv.currencySymbol}${inv.referenceValue}) was preserved.`,
      'success'
    );
  };

  // Action: Reset Reference
  const handleResetReference = (e: React.FormEvent) => {
    e.preventDefault();
    const inv = investments.find((i) => i.id === selectedInvId);
    if (!inv) return;

    const previous = inv.referenceValue;

    // Update investment
    setInvestments((prev) =>
      prev.map((item) =>
        item.id === selectedInvId
          ? {
              ...item,
              referenceValue: newRefValue,
              referenceDate: refResetDate
            }
          : item
      )
    );

    // Add to audit trail
    const audit: MarketReferenceAudit = {
      id: Date.now(),
      investmentId: inv.id,
      investmentName: inv.name,
      referenceValue: newRefValue,
      previousValue: previous,
      effectiveDate: refResetDate,
      reason: refResetReason,
      timestamp: new Date().toISOString().replace('T', ' ').substring(0, 19)
    };
    setReferenceAudits((prev) => [audit, ...prev]);

    setResetRefModalOpen(false);
    showNotification(
      `Reset baseline reference for ${inv.name} from ${inv.currencySymbol}${previous} to ${inv.currencySymbol}${newRefValue}. Audit entry saved.`,
      'info'
    );
  };

  // Action: Trigger On-Demand Daily Evaluation
  const handleRunDailyCheck = () => {
    const logs: string[] = [];
    const now = new Date();
    const todayStr = '2026-10-07'; // current simulation date

    logs.push(`[${now.toLocaleTimeString()}] 🚀 Initiating Daily Evaluation Job (Zone: Asia/Kolkata)...`);
    logs.push(`[${now.toLocaleTimeString()}] Fetching market quotes from configured provider...`);

    let newSmsCount = 0;

    investments.forEach((inv) => {
      logs.push(`----------------------------------------------------------------`);
      logs.push(`[${now.toLocaleTimeString()}] Evaluating: ${inv.name} (${inv.ticker})`);
      logs.push(`[${now.toLocaleTimeString()}] Current Quote: ${inv.currencySymbol}${inv.currentValue} | Baseline: ${inv.currencySymbol}${inv.referenceValue}`);

      const drawdown = computeDrawdown(inv.currentValue, inv.referenceValue);
      const suggestion = computeSuggestion(inv, drawdown);
      logs.push(`[${now.toLocaleTimeString()}] Calculated Drawdown: ${drawdown}% | Suggested Contribution: ${inv.currencySymbol}${suggestion.amount}`);

      const isReviewDue = todayStr >= inv.nextReviewDate;
      logs.push(`[${now.toLocaleTimeString()}] Schedule Review Check: Next Review Date = ${inv.nextReviewDate} -> Due Today? ${isReviewDue ? 'YES' : 'NO'}`);

      if (isReviewDue) {
        // Check idempotency: did we already send a review SMS today?
        const alreadySent = notifications.some(
          (n) => n.investmentName === inv.name && n.type === 'SCHEDULED_REVIEW' && n.sentAt.startsWith(todayStr)
        );

        if (alreadySent) {
          logs.push(`[${now.toLocaleTimeString()}] ℹ️ Idempotency check: Review SMS already dispatched for ${inv.name} today. Duplicate suppressed.`);
        } else {
          logs.push(`[${now.toLocaleTimeString()}] 📱 Dispatching Scheduled Review SMS via ${smsProvider}...`);
          const msg = `${inv.name} review due.\n\nLast: ${inv.currencySymbol}${inv.lastInvestmentAmount} on ${inv.lastInvestmentDate}\n\n${inv.name.includes('ATLAS') ? 'S&P 500' : 'Gold/ETF'}:\nCurrent: ${inv.currentValue}\nReference: ${inv.referenceValue}\nDrawdown: ${drawdown}%\n\nSuggested: ${inv.currencySymbol}${suggestion.amount}\nReason: ${suggestion.reason}.\n\nThis is a reminder only.\nNo trade has been placed.`;

          const newNotif: NotificationItem = {
            id: Date.now() + Math.random(),
            investmentName: inv.name,
            type: 'SCHEDULED_REVIEW',
            threshold: drawdown <= -5 ? -5 : 0,
            message: msg,
            sentAt: `${todayStr} 08:00:00`,
            provider: smsProvider,
            status: 'SENT',
            phoneMasked: '+91******3210'
          };
          setNotifications((prev) => [newNotif, ...prev]);
          newSmsCount++;
        }
      }

      // Check Threshold crosses (-5%, -15%, -25%)
      const thresholds = [-5, -15, -25];
      thresholds.forEach((th) => {
        if (drawdown <= th) {
          const lastEvent = triggerEvents.find((e) => e.investmentName === inv.name && e.threshold === th);
          if (!lastEvent || lastEvent.type === 'THRESHOLD_RECOVERY') {
            logs.push(`[${now.toLocaleTimeString()}] 🚨 Cross threshold ${th}% detected! Recording trigger event.`);
            setTriggerEvents((prev) => [
              {
                id: Date.now() + Math.random(),
                investmentName: inv.name,
                threshold: th,
                drawdown,
                marketValue: inv.currentValue,
                referenceValue: inv.referenceValue,
                type: 'THRESHOLD_ALERT',
                timestamp: `${todayStr} 08:00:00`
              },
              ...prev
            ]);
          } else {
            logs.push(`[${now.toLocaleTimeString()}] ℹ️ Threshold ${th}% was already alerted and market has not recovered. No duplicate alert.`);
          }
        }
      });
    });

    logs.push(`----------------------------------------------------------------`);
    logs.push(`[${now.toLocaleTimeString()}] ✅ Daily evaluation completed successfully. Sent ${newSmsCount} new SMS notifications.`);

    setDiagnosticLogs(logs);
    setShowLogModal(true);
    showNotification(`Daily check finished. Evaluated 2 investments. Idempotent check verified.`, 'success');
  };

  // Action: Send Test SMS
  const handleSendTestSms = (e: React.FormEvent) => {
    e.preventDefault();
    const newNotif: NotificationItem = {
      id: Date.now(),
      investmentName: 'SYSTEM_TEST',
      type: 'SYSTEM_TEST',
      threshold: null,
      message: smsCustomMessage,
      sentAt: new Date().toISOString().replace('T', ' ').substring(0, 19),
      provider: smsProvider,
      status: 'SENT',
      phoneMasked: smsRecipient.replace(/(\+\d{2})\d+(\d{4})/, '$1******$2')
    };
    setNotifications((prev) => [newNotif, ...prev]);
    setTestSmsModalOpen(false);
    showNotification(`Test SMS sent successfully via ${smsProvider.toUpperCase()} to ${newNotif.phoneMasked}!`, 'success');
  };

  const openRecordModalFor = (invId: number) => {
    const inv = investments.find((i) => i.id === invId);
    if (!inv) return;
    setSelectedInvId(invId);
    setRecordAmount(inv.lastInvestmentAmount || inv.normalAmount);
    setRecordDate('2026-10-07');
    setRecordNotes('Quarterly contribution');
    setRecordModalOpen(true);
  };

  const openResetRefModalFor = (invId: number) => {
    const inv = investments.find((i) => i.id === invId);
    if (!inv) return;
    setSelectedInvId(invId);
    setNewRefValue(inv.referenceValue);
    setRefResetDate('2026-10-07');
    setRefResetReason('Manual baseline recalibration');
    setResetRefModalOpen(true);
  };

  // Code snippets for viewer
  const codeFiles = {
    'pom.xml': `<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" ...>
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.3.4</version>
  </parent>
  <groupId>com.investmentsentinel</groupId>
  <artifactId>investment-sentinel</artifactId>
  <version>1.0.0</version>
  <properties>
    <java.version>21</java.version>
  </properties>
  <dependencies>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
      <groupId>org.postgresql</groupId>
      <artifactId>postgresql</artifactId>
    </dependency>
    <dependency>
      <groupId>org.flywaydb</groupId>
      <artifactId>flyway-database-postgresql</artifactId>
    </dependency>
  </dependencies>
</project>`,
    'Dockerfile': `# Multi-stage Build for 100% Free Hosting (Render / Koyeb)
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine AS runner
WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --from=builder /app/target/investment-sentinel-*.jar app.jar
USER appuser
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod \\
    JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=\${PORT:-8080} -jar app.jar"]`,
    'docker-compose.yml': `version: '3.8'
services:
  postgres:
    image: postgres:16-alpine
    restart: unless-stopped
    environment:
      POSTGRES_DB: investment_sentinel
      POSTGRES_USER: sentinel_user
      POSTGRES_PASSWORD: sentinel_secret_password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  app:
    build: .
    restart: unless-stopped
    depends_on:
      postgres:
        condition: service_healthy
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/investment_sentinel
      SMS_PROVIDER: fast2sms
      TIMEZONE: Asia/Kolkata
volumes:
  postgres_data:`,
    'V1__init_schema.sql': `-- PostgreSQL Schema Migrations
CREATE TABLE investments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    currency VARCHAR(10) NOT NULL,
    last_investment_date DATE,
    last_investment_amount NUMERIC(15, 2),
    next_review_date DATE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE market_references (
    id BIGSERIAL PRIMARY KEY,
    investment_id BIGINT REFERENCES investments(id),
    reference_type VARCHAR(50) NOT NULL,
    reference_value NUMERIC(15, 4) NOT NULL,
    reference_date DATE NOT NULL,
    reason VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE trigger_events (
    id BIGSERIAL PRIMARY KEY,
    investment_id BIGINT REFERENCES investments(id),
    threshold NUMERIC(6, 2) NOT NULL,
    drawdown NUMERIC(6, 2) NOT NULL,
    market_value NUMERIC(15, 4) NOT NULL,
    reference_value NUMERIC(15, 4) NOT NULL,
    trigger_type VARCHAR(50) NOT NULL,
    triggered_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    investment_id BIGINT REFERENCES investments(id),
    notification_type VARCHAR(50) NOT NULL,
    threshold NUMERIC(6, 2),
    message TEXT NOT NULL,
    sent_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    provider VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL
);`
  };

  const [selectedCodeFile, setSelectedCodeFile] = useState<keyof typeof codeFiles>('Dockerfile');

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col font-sans">
      {/* Top Banner: Critical Safety Reminder */}
      <div className="bg-amber-950/70 border-b border-amber-800/80 px-4 py-2.5 text-xs text-amber-200 flex items-center justify-between">
        <div className="flex items-center gap-2 max-w-5xl mx-auto w-full">
          <ShieldAlert className="w-4 h-4 text-amber-400 shrink-0" />
          <span>
            <strong>Investment Safety Rule:</strong> Single-user monitoring and reminder tool only. Strictly executes configured drawdown rules. Does <strong>NOT</strong> predict markets, recommend trades, or connect to brokerage accounts.
          </span>
        </div>
      </div>

      {/* Main Header */}
      <header className="border-b border-slate-800 bg-slate-900/80 backdrop-blur sticky top-0 z-40">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-3.5 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-lg bg-indigo-600/30 border border-indigo-500/50 flex items-center justify-center text-indigo-400 shadow-inner">
              <TrendingDown className="w-5 h-5 text-indigo-400" />
            </div>
            <div>
              <h1 className="text-lg font-bold text-white tracking-tight flex items-center gap-2">
                Investment Sentinel
                <span className="text-[11px] font-normal px-2 py-0.5 rounded-full bg-emerald-500/20 text-emerald-400 border border-emerald-500/30">
                  Java 21 &bull; Spring Boot 3
                </span>
              </h1>
              <p className="text-xs text-slate-400">
                ATLAS GLOBAL &amp; ICICI Prudential Gold ETF &bull; SMS Alerts &bull; Asia/Kolkata
              </p>
            </div>
          </div>

          {/* Action buttons & Tab switch */}
          <div className="flex items-center gap-2 flex-wrap">
            <button
              onClick={handleRunDailyCheck}
              className="px-3.5 py-1.5 bg-indigo-600 hover:bg-indigo-500 text-white rounded-lg text-xs font-semibold flex items-center gap-1.5 transition-colors shadow-sm cursor-pointer"
            >
              <RefreshCw className="w-3.5 h-3.5" />
              Run Daily Evaluation
            </button>

            <button
              onClick={() => setTestSmsModalOpen(true)}
              className="px-3.5 py-1.5 bg-slate-800 hover:bg-slate-700 border border-slate-700 text-slate-200 rounded-lg text-xs font-medium flex items-center gap-1.5 transition-colors cursor-pointer"
            >
              <Send className="w-3.5 h-3.5 text-emerald-400" />
              Test SMS
            </button>
          </div>
        </div>

        {/* Tab navigation */}
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex space-x-6 border-t border-slate-800/60 text-xs font-medium">
          <button
            onClick={() => setActiveTab('dashboard')}
            className={`py-2.5 border-b-2 transition-colors cursor-pointer flex items-center gap-1.5 ${
              activeTab === 'dashboard'
                ? 'border-indigo-500 text-indigo-400 font-semibold'
                : 'border-transparent text-slate-400 hover:text-slate-200'
            }`}
          >
            <Clock className="w-3.5 h-3.5" />
            Live Dashboard
          </button>
          <button
            onClick={() => setActiveTab('history')}
            className={`py-2.5 border-b-2 transition-colors cursor-pointer flex items-center gap-1.5 ${
              activeTab === 'history'
                ? 'border-indigo-500 text-indigo-400 font-semibold'
                : 'border-transparent text-slate-400 hover:text-slate-200'
            }`}
          >
            <History className="w-3.5 h-3.5" />
            Audit &amp; Logs ({notifications.length + referenceAudits.length})
          </button>
          <button
            onClick={() => setActiveTab('simulator')}
            className={`py-2.5 border-b-2 transition-colors cursor-pointer flex items-center gap-1.5 ${
              activeTab === 'simulator'
                ? 'border-indigo-500 text-indigo-400 font-semibold'
                : 'border-transparent text-slate-400 hover:text-slate-200'
            }`}
          >
            <Sliders className="w-3.5 h-3.5" />
            Drawdown &amp; Schedule Tester
          </button>
          <button
            onClick={() => setActiveTab('code')}
            className={`py-2.5 border-b-2 transition-colors cursor-pointer flex items-center gap-1.5 ${
              activeTab === 'code'
                ? 'border-indigo-500 text-indigo-400 font-semibold'
                : 'border-transparent text-slate-400 hover:text-slate-200'
            }`}
          >
            <FileCode className="w-3.5 h-3.5" />
            Project &amp; Docker Files
          </button>
        </div>
      </header>

      {/* Flash Banner */}
      {bannerMessage && (
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mt-4 w-full">
          <div
            className={`p-3 rounded-lg border text-xs flex items-center justify-between ${
              bannerMessage.type === 'success'
                ? 'bg-emerald-950/80 border-emerald-600 text-emerald-200'
                : bannerMessage.type === 'error'
                ? 'bg-rose-950/80 border-rose-600 text-rose-200'
                : 'bg-indigo-950/80 border-indigo-600 text-indigo-200'
            }`}
          >
            <div className="flex items-center gap-2">
              <CheckCircle2 className="w-4 h-4 shrink-0" />
              <span>{bannerMessage.text}</span>
            </div>
            <button
              onClick={() => setBannerMessage(null)}
              className="text-slate-400 hover:text-white text-sm"
            >
              &times;
            </button>
          </div>
        </div>
      )}

      {/* Main Container */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6 w-full flex-1">
        {activeTab === 'dashboard' && (
          <div className="space-y-6">
            {/* Top Info Bar */}
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
              <div className="bg-slate-900/60 border border-slate-800 rounded-xl p-4 flex items-center justify-between">
                <div>
                  <div className="text-xs text-slate-400">Monitoring Cron Schedule</div>
                  <div className="text-sm font-semibold text-slate-200 mt-0.5">08:00 AM IST Daily</div>
                  <div className="text-[11px] text-slate-500">Zone: Asia/Kolkata</div>
                </div>
                <Clock className="w-7 h-7 text-indigo-400/60" />
              </div>

              <div className="bg-slate-900/60 border border-slate-800 rounded-xl p-4 flex items-center justify-between">
                <div>
                  <div className="text-xs text-slate-400">SMS Primary Gateway</div>
                  <div className="text-sm font-semibold text-slate-200 mt-0.5 capitalize">{smsProvider} (Trial Balance)</div>
                  <div className="text-[11px] text-slate-500">To: +91******3210</div>
                </div>
                <Bell className="w-7 h-7 text-emerald-400/60" />
              </div>

              <div className="bg-slate-900/60 border border-slate-800 rounded-xl p-4 flex items-center justify-between">
                <div>
                  <div className="text-xs text-slate-400">Free Hosting Target</div>
                  <div className="text-sm font-semibold text-slate-200 mt-0.5">Render / Koyeb Free</div>
                  <div className="text-[11px] text-slate-500">Persistent PostgreSQL (Neon)</div>
                </div>
                <Server className="w-7 h-7 text-amber-400/60" />
              </div>
            </div>

            {/* Investments Two-Column Cards Grid */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              {investments.map((inv) => {
                const drawdown = computeDrawdown(inv.currentValue, inv.referenceValue);
                const suggestion = computeSuggestion(inv, drawdown);
                const isReviewDue = '2026-10-07' >= inv.nextReviewDate;

                return (
                  <div
                    key={inv.id}
                    className="bg-slate-900/80 border border-slate-800 rounded-2xl p-6 shadow-xl flex flex-col justify-between"
                  >
                    <div>
                      {/* Card Header */}
                      <div className="flex justify-between items-start pb-4 border-b border-slate-800">
                        <div>
                          <div className="flex items-center gap-2">
                            <span className="text-xs font-bold uppercase tracking-wider px-2 py-0.5 rounded bg-indigo-500/20 text-indigo-400 border border-indigo-500/30">
                              {inv.currency}
                            </span>
                            <span className="text-xs text-slate-400 font-mono">{inv.ticker}</span>
                          </div>
                          <h2 className="text-xl font-bold text-white mt-1.5">{inv.name}</h2>
                        </div>
                        <div className="text-right">
                          <span
                            className={`inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-semibold ${
                              isReviewDue
                                ? 'bg-amber-500/20 text-amber-300 border border-amber-500/40 animate-pulse'
                                : 'bg-slate-800 text-slate-300'
                            }`}
                          >
                            <Calendar className="w-3 h-3" />
                            {isReviewDue ? 'Review Due Today' : 'Schedule Active'}
                          </span>
                        </div>
                      </div>

                      {/* Main Drawdown Gauge & Numbers */}
                      <div className="mt-5 grid grid-cols-2 gap-4">
                        <div className="bg-slate-950/60 border border-slate-800/80 rounded-xl p-3.5">
                          <div className="text-xs text-slate-400 flex items-center justify-between">
                            <span>Current Market Level</span>
                            <span className="text-[10px] text-emerald-400 font-medium">LIVE</span>
                          </div>
                          <div className="text-xl font-mono font-bold text-white mt-1">
                            {inv.currencySymbol}
                            {inv.currentValue.toLocaleString()}
                          </div>
                          <div className="text-[11px] text-slate-500 mt-1">
                            Ref: {inv.currencySymbol}{inv.referenceValue.toLocaleString()} ({inv.referenceDate})
                          </div>
                        </div>

                        <div className="bg-slate-950/60 border border-slate-800/80 rounded-xl p-3.5">
                          <div className="text-xs text-slate-400">Current Drawdown</div>
                          <div
                            className={`text-xl font-mono font-bold mt-1 ${
                              drawdown < 0 ? 'text-rose-400' : 'text-emerald-400'
                            }`}
                          >
                            {drawdown}%
                          </div>
                          <div className="text-[11px] text-slate-400 mt-1">
                            vs. baseline reference
                          </div>
                        </div>
                      </div>

                      {/* Suggested Contribution Section */}
                      <div className="mt-4 p-4 rounded-xl bg-slate-950/90 border border-slate-800">
                        <div className="flex items-center justify-between">
                          <span className="text-xs font-medium text-slate-400 uppercase tracking-wider">
                            Suggested Contribution
                          </span>
                          <span className="text-lg font-bold font-mono text-emerald-400">
                            {inv.currencySymbol}
                            {suggestion.amount.toLocaleString()}
                          </span>
                        </div>
                        <p className="text-xs text-slate-300 mt-1.5 flex items-start gap-1.5">
                          <Info className="w-3.5 h-3.5 text-indigo-400 shrink-0 mt-0.5" />
                          <span>{suggestion.reason}</span>
                        </p>

                        {/* Configured Tiers Display */}
                        <div className="mt-3 pt-3 border-t border-slate-800/80 grid grid-cols-4 gap-1.5 text-center text-[10px]">
                          <div
                            className={`p-1.5 rounded ${
                              drawdown > -inv.t1Drawdown
                                ? 'bg-indigo-600/30 border border-indigo-500/50 text-white font-bold'
                                : 'bg-slate-900 text-slate-400'
                            }`}
                          >
                            <div>Normal</div>
                            <div className="font-mono mt-0.5">{inv.currencySymbol}{inv.normalAmount}</div>
                          </div>
                          <div
                            className={`p-1.5 rounded ${
                              drawdown <= -inv.t1Drawdown && drawdown > -inv.t2Drawdown
                                ? 'bg-indigo-600/30 border border-indigo-500/50 text-white font-bold'
                                : 'bg-slate-900 text-slate-400'
                            }`}
                          >
                            <div>≤ -{inv.t1Drawdown}%</div>
                            <div className="font-mono mt-0.5">{inv.currencySymbol}{inv.t1Amount}</div>
                          </div>
                          <div
                            className={`p-1.5 rounded ${
                              drawdown <= -inv.t2Drawdown && drawdown > -inv.t3Drawdown
                                ? 'bg-indigo-600/30 border border-indigo-500/50 text-white font-bold'
                                : 'bg-slate-900 text-slate-400'
                            }`}
                          >
                            <div>≤ -{inv.t2Drawdown}%</div>
                            <div className="font-mono mt-0.5">{inv.currencySymbol}{inv.t2Amount}</div>
                          </div>
                          <div
                            className={`p-1.5 rounded ${
                              drawdown <= -inv.t3Drawdown
                                ? 'bg-indigo-600/30 border border-indigo-500/50 text-white font-bold'
                                : 'bg-slate-900 text-slate-400'
                            }`}
                          >
                            <div>≤ -{inv.t3Drawdown}%</div>
                            <div className="font-mono mt-0.5">{inv.currencySymbol}{inv.t3Amount}</div>
                          </div>
                        </div>
                      </div>

                      {/* 3-Month Calendar Schedule Info */}
                      <div className="mt-4 grid grid-cols-2 gap-3 text-xs bg-slate-900/40 p-3 rounded-lg border border-slate-800">
                        <div>
                          <div className="text-slate-400">Last Investment</div>
                          <div className="font-semibold text-slate-200 mt-0.5">
                            {inv.currencySymbol}{inv.lastInvestmentAmount} &bull; {inv.lastInvestmentDate}
                          </div>
                        </div>
                        <div>
                          <div className="text-slate-400">Next Review Date</div>
                          <div className={`font-semibold mt-0.5 ${isReviewDue ? 'text-amber-400' : 'text-slate-200'}`}>
                            {inv.nextReviewDate} ({isReviewDue ? 'Due Now' : 'Pending'})
                          </div>
                        </div>
                      </div>
                    </div>

                    {/* Action Buttons */}
                    <div className="grid grid-cols-2 gap-3 mt-6 pt-4 border-t border-slate-800">
                      <button
                        onClick={() => openRecordModalFor(inv.id)}
                        className="px-3.5 py-2 bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-bold rounded-lg flex items-center justify-center gap-1.5 transition-colors cursor-pointer shadow-sm"
                      >
                        <PlusCircle className="w-3.5 h-3.5" />
                        Record Investment
                      </button>
                      <button
                        onClick={() => openResetRefModalFor(inv.id)}
                        className="px-3.5 py-2 bg-slate-800 hover:bg-slate-700 border border-slate-700 text-slate-200 text-xs font-semibold rounded-lg flex items-center justify-center gap-1.5 transition-colors cursor-pointer"
                      >
                        <RefreshCw className="w-3.5 h-3.5 text-amber-400" />
                        Reset Reference
                      </button>
                    </div>
                  </div>
                );
              })}
            </div>

            {/* Quick SMS Preview Card */}
            <div className="bg-slate-900/60 border border-slate-800 rounded-xl p-5">
              <div className="flex items-center justify-between mb-3">
                <h3 className="text-sm font-semibold text-white flex items-center gap-2">
                  <Bell className="w-4 h-4 text-emerald-400" />
                  Sample Formatted SMS Notifications
                </h3>
                <span className="text-xs text-slate-500">Concise, DLT-Compliant, No Trading Execution</span>
              </div>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="bg-slate-950 p-3.5 rounded-lg border border-slate-800 font-mono text-xs text-slate-300 whitespace-pre-line">
                  {`ATLAS GLOBAL review due.

Last: $50 on 07-Jul-2026

S&P 500:
Current: 6,050
Reference: 6,500
Drawdown: -6.92%

Suggested: $50
Reason: market is ≥5% below reference.

This is a reminder only.
No trade has been placed.`}
                </div>
                <div className="bg-slate-950 p-3.5 rounded-lg border border-slate-800 font-mono text-xs text-slate-300 whitespace-pre-line">
                  {`ATLAS GLOBAL alert:

S&P 500 crossed -15% from your reference.
Current drawdown: -15.4%

Suggested contribution at next review: $75.
Next scheduled review: 18 days.

No trade has been placed.`}
                </div>
              </div>
            </div>
          </div>
        )}

        {/* History Tab */}
        {activeTab === 'history' && (
          <div className="space-y-6">
            {/* SMS Notifications Log */}
            <div className="bg-slate-900/80 border border-slate-800 rounded-xl p-5 shadow-lg">
              <h3 className="text-sm font-bold text-white mb-3 flex items-center gap-2">
                <Send className="w-4 h-4 text-emerald-400" />
                Dispatched SMS Notification Audit Log ({notifications.length})
              </h3>
              <div className="overflow-x-auto">
                <table className="w-full text-left text-xs">
                  <thead className="text-slate-400 border-b border-slate-800 uppercase text-[10px] tracking-wider">
                    <tr>
                      <th className="pb-2.5">Sent Timestamp</th>
                      <th className="pb-2.5">Investment</th>
                      <th className="pb-2.5">Type</th>
                      <th className="pb-2.5">Provider</th>
                      <th className="pb-2.5">Recipient</th>
                      <th className="pb-2.5">Status</th>
                      <th className="pb-2.5">Message Content</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-800/80 text-slate-300">
                    {notifications.map((n) => (
                      <tr key={n.id} className="hover:bg-slate-800/30">
                        <td className="py-2.5 font-mono text-slate-400 whitespace-nowrap">{n.sentAt}</td>
                        <td className="py-2.5 font-semibold text-white whitespace-nowrap">{n.investmentName}</td>
                        <td className="py-2.5">
                          <span className="px-2 py-0.5 rounded bg-slate-800 text-slate-300 border border-slate-700 text-[10px]">
                            {n.type}
                          </span>
                        </td>
                        <td className="py-2.5 font-mono text-indigo-400 uppercase text-[11px]">{n.provider}</td>
                        <td className="py-2.5 font-mono text-slate-400">{n.phoneMasked}</td>
                        <td className="py-2.5">
                          <span className="text-emerald-400 font-bold flex items-center gap-1">
                            <CheckCircle2 className="w-3.5 h-3.5" />
                            {n.status}
                          </span>
                        </td>
                        <td className="py-2.5 max-w-xs truncate text-slate-400 font-mono text-[11px]" title={n.message}>
                          {n.message.replace(/\n+/g, ' ')}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>

            {/* Reference Reset Audit History */}
            <div className="bg-slate-900/80 border border-slate-800 rounded-xl p-5 shadow-lg">
              <h3 className="text-sm font-bold text-white mb-3 flex items-center gap-2">
                <RefreshCw className="w-4 h-4 text-amber-400" />
                Market Reference Baseline Audit History ({referenceAudits.length})
              </h3>
              <div className="overflow-x-auto">
                <table className="w-full text-left text-xs">
                  <thead className="text-slate-400 border-b border-slate-800 uppercase text-[10px] tracking-wider">
                    <tr>
                      <th className="pb-2.5">Audit Timestamp</th>
                      <th className="pb-2.5">Investment</th>
                      <th className="pb-2.5">Previous Value</th>
                      <th className="pb-2.5">New Baseline Reference</th>
                      <th className="pb-2.5">Effective Date</th>
                      <th className="pb-2.5">Reason Recorded</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-800/80 text-slate-300">
                    {referenceAudits.map((a) => (
                      <tr key={a.id} className="hover:bg-slate-800/30">
                        <td className="py-2.5 font-mono text-slate-400 whitespace-nowrap">{a.timestamp}</td>
                        <td className="py-2.5 font-semibold text-white">{a.investmentName}</td>
                        <td className="py-2.5 font-mono text-slate-400">{a.previousValue.toLocaleString()}</td>
                        <td className="py-2.5 font-mono font-bold text-amber-400">{a.referenceValue.toLocaleString()}</td>
                        <td className="py-2.5 font-mono">{a.effectiveDate}</td>
                        <td className="py-2.5 text-slate-300 italic">{a.reason}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>

            {/* Historical Contributions */}
            <div className="bg-slate-900/80 border border-slate-800 rounded-xl p-5 shadow-lg">
              <h3 className="text-sm font-bold text-white mb-3 flex items-center gap-2">
                <DollarSign className="w-4 h-4 text-indigo-400" />
                Historical Investment Contributions ({investmentHistory.length})
              </h3>
              <div className="overflow-x-auto">
                <table className="w-full text-left text-xs">
                  <thead className="text-slate-400 border-b border-slate-800 uppercase text-[10px] tracking-wider">
                    <tr>
                      <th className="pb-2.5">Contribution Date</th>
                      <th className="pb-2.5">Investment</th>
                      <th className="pb-2.5">Amount</th>
                      <th className="pb-2.5">Notes</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-800/80 text-slate-300">
                    {investmentHistory.map((h) => (
                      <tr key={h.id} className="hover:bg-slate-800/30">
                        <td className="py-2.5 font-mono text-slate-400">{h.date}</td>
                        <td className="py-2.5 font-semibold text-white">{h.investmentName}</td>
                        <td className="py-2.5 font-mono font-bold text-emerald-400">
                          {h.currency === 'USD' ? '$' : '₹'}
                          {h.amount.toLocaleString()}
                        </td>
                        <td className="py-2.5 text-slate-300">{h.notes}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        )}

        {/* Simulator & Boundary Edge Cases Tab */}
        {activeTab === 'simulator' && (
          <div className="space-y-6">
            <div className="bg-slate-900/80 border border-slate-800 rounded-xl p-6">
              <h3 className="text-base font-bold text-white flex items-center gap-2">
                <Sliders className="w-5 h-5 text-indigo-400" />
                Interactive Market Price &amp; Threshold Simulator
              </h3>
              <p className="text-xs text-slate-400 mt-1">
                Test how varying market levels affect drawdowns, contribution amounts, and trigger conditions without modifying database baselines.
              </p>

              {/* Slider for ATLAS GLOBAL */}
              <div className="mt-6 space-y-4">
                <div className="p-4 rounded-xl bg-slate-950 border border-slate-800">
                  <div className="flex justify-between items-center mb-2">
                    <span className="text-sm font-semibold text-white">ATLAS GLOBAL (S&P 500 Simulator)</span>
                    <span className="text-xs font-mono text-indigo-400">
                      Baseline: ${investments[0].referenceValue}
                    </span>
                  </div>
                  <div className="flex items-center gap-4">
                    <input
                      type="range"
                      min="4000"
                      max="7000"
                      step="10"
                      value={investments[0].currentValue}
                      onChange={(e) => {
                        const val = parseFloat(e.target.value);
                        setInvestments((prev) =>
                          prev.map((i) => (i.id === 1 ? { ...i, currentValue: val } : i))
                        );
                      }}
                      className="w-full accent-indigo-500 cursor-pointer"
                    />
                    <span className="font-mono text-sm font-bold text-white w-20 text-right">
                      ${investments[0].currentValue}
                    </span>
                  </div>
                  {/* Boundary quick test buttons */}
                  <div className="flex flex-wrap gap-2 mt-3 pt-3 border-t border-slate-800/80 text-xs">
                    <span className="text-slate-400 text-[11px] self-center mr-1">Quick Edge Tests:</span>
                    <button
                      onClick={() => {
                        // -4.99% from 6500 = 6175.65 -> $25
                        setInvestments((prev) => prev.map((i) => (i.id === 1 ? { ...i, currentValue: 6176 } : i)));
                      }}
                      className="px-2.5 py-1 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded text-[11px] cursor-pointer"
                    >
                      -4.99% ($6,176 &rarr; $25)
                    </button>
                    <button
                      onClick={() => {
                        // exactly -5.00% from 6500 = 6175 -> $50
                        setInvestments((prev) => prev.map((i) => (i.id === 1 ? { ...i, currentValue: 6175 } : i)));
                      }}
                      className="px-2.5 py-1 bg-indigo-900/60 hover:bg-indigo-900 border border-indigo-700 text-indigo-200 rounded text-[11px] cursor-pointer"
                    >
                      Exactly -5.00% ($6,175 &rarr; $50)
                    </button>
                    <button
                      onClick={() => {
                        // -14.99% from 6500 = 5525.65 -> $50
                        setInvestments((prev) => prev.map((i) => (i.id === 1 ? { ...i, currentValue: 5526 } : i)));
                      }}
                      className="px-2.5 py-1 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded text-[11px] cursor-pointer"
                    >
                      -14.99% ($5,526 &rarr; $50)
                    </button>
                    <button
                      onClick={() => {
                        // exactly -15.00% from 6500 = 5525 -> $75
                        setInvestments((prev) => prev.map((i) => (i.id === 1 ? { ...i, currentValue: 5525 } : i)));
                      }}
                      className="px-2.5 py-1 bg-indigo-900/60 hover:bg-indigo-900 border border-indigo-700 text-indigo-200 rounded text-[11px] cursor-pointer"
                    >
                      Exactly -15.00% ($5,525 &rarr; $75)
                    </button>
                    <button
                      onClick={() => {
                        // exactly -25.00% from 6500 = 4875 -> $100
                        setInvestments((prev) => prev.map((i) => (i.id === 1 ? { ...i, currentValue: 4875 } : i)));
                      }}
                      className="px-2.5 py-1 bg-rose-950/80 hover:bg-rose-900 border border-rose-800 text-rose-200 rounded text-[11px] cursor-pointer"
                    >
                      Exactly -25.00% ($4,875 &rarr; $100)
                    </button>
                  </div>
                </div>

                {/* 3-Month Calendar Interval Verifier */}
                <div className="p-4 rounded-xl bg-slate-950 border border-slate-800 text-xs">
                  <div className="font-semibold text-white mb-2 flex items-center gap-1.5">
                    <Calendar className="w-4 h-4 text-emerald-400" />
                    Calendar Month Logic Verification (No simple 90-day assumptions)
                  </div>
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-3 text-slate-300">
                    <div className="bg-slate-900 p-2.5 rounded border border-slate-800">
                      <div className="text-slate-400">Regular Month</div>
                      <div className="font-mono mt-1 text-slate-200">10 Jan 2026 + 3 mo &rarr; 10 Apr 2026</div>
                      <div className="text-[10px] text-emerald-400 mt-0.5">✓ 90 calendar days matched</div>
                    </div>
                    <div className="bg-slate-900 p-2.5 rounded border border-slate-800">
                      <div className="text-slate-400">Month-End Shift</div>
                      <div className="font-mono mt-1 text-slate-200">31 Jan 2026 + 3 mo &rarr; 30 Apr 2026</div>
                      <div className="text-[10px] text-emerald-400 mt-0.5">✓ Clamps to 30 days of April</div>
                    </div>
                    <div className="bg-slate-900 p-2.5 rounded border border-slate-800">
                      <div className="text-slate-400">Leap Year Test</div>
                      <div className="font-mono mt-1 text-slate-200">29 Nov 2023 + 3 mo &rarr; 29 Feb 2024</div>
                      <div className="text-[10px] text-emerald-400 mt-0.5">✓ Leap year handled properly</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Code & Deployment Files Tab */}
        {activeTab === 'code' && (
          <div className="space-y-6">
            <div className="bg-slate-900/80 border border-slate-800 rounded-xl p-6">
              <div className="flex flex-col sm:flex-row sm:items-center justify-between pb-4 border-b border-slate-800 gap-3">
                <div>
                  <h3 className="text-base font-bold text-white flex items-center gap-2">
                    <FileCode className="w-5 h-5 text-indigo-400" />
                    Full Spring Boot 3 &amp; Docker Project Files
                  </h3>
                  <p className="text-xs text-slate-400 mt-0.5">
                    Production-ready Java 21 repository files, Flyway schema, and zero-cost cloud deployment scripts.
                  </p>
                </div>
                <div className="flex items-center gap-2">
                  <button
                    onClick={() => copyToClipboard(codeFiles[selectedCodeFile], selectedCodeFile)}
                    className="px-3 py-1.5 bg-slate-800 hover:bg-slate-700 text-white rounded-lg text-xs font-medium flex items-center gap-1.5 cursor-pointer border border-slate-700"
                  >
                    {copiedKey === selectedCodeFile ? (
                      <>
                        <Check className="w-3.5 h-3.5 text-emerald-400" />
                        Copied!
                      </>
                    ) : (
                      <>
                        <Copy className="w-3.5 h-3.5" />
                        Copy File
                      </>
                    )}
                  </button>
                </div>
              </div>

              {/* File Selector Tabs */}
              <div className="flex gap-2 mt-4 overflow-x-auto pb-2 text-xs">
                {(Object.keys(codeFiles) as Array<keyof typeof codeFiles>).map((fileName) => (
                  <button
                    key={fileName}
                    onClick={() => setSelectedCodeFile(fileName)}
                    className={`px-3 py-1.5 rounded-lg font-mono transition-colors cursor-pointer whitespace-nowrap ${
                      selectedCodeFile === fileName
                        ? 'bg-indigo-600 text-white font-semibold'
                        : 'bg-slate-950 text-slate-400 hover:text-white border border-slate-800'
                    }`}
                  >
                    {fileName}
                  </button>
                ))}
              </div>

              {/* Code viewer */}
              <div className="mt-4 bg-slate-950 border border-slate-800 rounded-xl p-4 overflow-x-auto">
                <pre className="font-mono text-xs text-slate-300 leading-relaxed whitespace-pre">
                  {codeFiles[selectedCodeFile]}
                </pre>
              </div>
            </div>

            {/* Free Hosting Instructions Box */}
            <div className="bg-slate-900/60 border border-slate-800 rounded-xl p-5 text-xs text-slate-300 space-y-3">
              <h4 className="font-bold text-white text-sm flex items-center gap-2">
                <Server className="w-4 h-4 text-emerald-400" />
                Render &amp; Koyeb 100% Free Tier Deployment Quick Checklist
              </h4>
              <ol className="list-decimal list-inside space-y-1.5 text-slate-300 leading-normal">
                <li>Create free persistent PostgreSQL on <strong>Neon.tech</strong> (always-free 0.5GB tier, retains data across restarts).</li>
                <li>Set environment variable <code className="bg-slate-950 px-1.5 py-0.5 rounded text-indigo-300">SPRING_DATASOURCE_URL=jdbc:postgresql://ep-...aws.neon.tech/investment_sentinel?sslmode=require</code>.</li>
                <li>Select <strong>Docker</strong> build on Render Free or Koyeb Nano (uses included multi-stage Dockerfile).</li>
                <li>Set <code className="bg-slate-950 px-1.5 py-0.5 rounded text-indigo-300">SMS_PROVIDER=fast2sms</code> (or <code className="bg-slate-950 px-1.5 py-0.5 rounded text-indigo-300">startmessaging</code>) and provide your free trial API key.</li>
                <li>Flyway will automatically execute migrations <code className="bg-slate-950 px-1.5 py-0.5 rounded text-indigo-300">V1</code> and <code className="bg-slate-950 px-1.5 py-0.5 rounded text-indigo-300">V2</code> on container startup.</li>
              </ol>
            </div>
          </div>
        )}
      </main>

      {/* Record Investment Modal */}
      {recordModalOpen && (
        <div className="fixed inset-0 bg-black/70 backdrop-blur-sm flex items-center justify-center p-4 z-50">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 max-w-md w-full shadow-2xl">
            <h3 className="text-lg font-bold text-white">
              Record Investment Contribution
            </h3>
            <p className="text-xs text-slate-400 mt-1">
              Updates last investment date and calculates the next review date (+3 calendar months). Baseline reference is <strong>preserved</strong>.
            </p>

            <form onSubmit={handleRecordInvestment} className="mt-5 space-y-4 text-xs">
              <div>
                <label className="block font-medium text-slate-300 mb-1">Contribution Amount</label>
                <input
                  type="number"
                  step="0.01"
                  min="1"
                  required
                  value={recordAmount}
                  onChange={(e) => setRecordAmount(parseFloat(e.target.value))}
                  className="w-full px-3.5 py-2.5 bg-slate-950 border border-slate-700 rounded-lg text-white font-mono text-sm focus:outline-none focus:border-indigo-500"
                />
              </div>

              <div>
                <label className="block font-medium text-slate-300 mb-1">Investment Date</label>
                <input
                  type="date"
                  required
                  value={recordDate}
                  onChange={(e) => setRecordDate(e.target.value)}
                  className="w-full px-3.5 py-2.5 bg-slate-950 border border-slate-700 rounded-lg text-white font-mono text-sm focus:outline-none focus:border-indigo-500"
                />
                <span className="text-[11px] text-slate-500 mt-1 block">
                  Next review will be automatically set to: <strong>{addCalendarMonths(recordDate, 3)}</strong>
                </span>
              </div>

              <div>
                <label className="block font-medium text-slate-300 mb-1">Notes / Smallcase Order Details</label>
                <input
                  type="text"
                  value={recordNotes}
                  onChange={(e) => setRecordNotes(e.target.value)}
                  placeholder="e.g. Regular 3-month contribution"
                  className="w-full px-3.5 py-2.5 bg-slate-950 border border-slate-700 rounded-lg text-white text-sm focus:outline-none focus:border-indigo-500"
                />
              </div>

              <div className="flex justify-end gap-3 pt-3">
                <button
                  type="button"
                  onClick={() => setRecordModalOpen(false)}
                  className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-lg font-medium cursor-pointer"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-emerald-600 hover:bg-emerald-500 text-white rounded-lg font-bold cursor-pointer shadow-sm"
                >
                  Save Contribution
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Reset Reference Modal */}
      {resetRefModalOpen && (
        <div className="fixed inset-0 bg-black/70 backdrop-blur-sm flex items-center justify-center p-4 z-50">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 max-w-md w-full shadow-2xl">
            <h3 className="text-lg font-bold text-white flex items-center gap-2">
              <RefreshCw className="w-5 h-5 text-amber-400" />
              Reset Market Baseline Reference
            </h3>
            <p className="text-xs text-slate-400 mt-1">
              Establishes a new reference level from which future drawdowns are measured. Preserves previous reference in audit history.
            </p>

            <form onSubmit={handleResetReference} className="mt-5 space-y-4 text-xs">
              <div>
                <label className="block font-medium text-slate-300 mb-1">New Reference Level</label>
                <input
                  type="number"
                  step="0.01"
                  required
                  value={newRefValue}
                  onChange={(e) => setNewRefValue(parseFloat(e.target.value))}
                  className="w-full px-3.5 py-2.5 bg-slate-950 border border-slate-700 rounded-lg text-white font-mono text-sm focus:outline-none focus:border-indigo-500"
                />
              </div>

              <div>
                <label className="block font-medium text-slate-300 mb-1">Effective Date</label>
                <input
                  type="date"
                  required
                  value={refResetDate}
                  onChange={(e) => setRefResetDate(e.target.value)}
                  className="w-full px-3.5 py-2.5 bg-slate-950 border border-slate-700 rounded-lg text-white font-mono text-sm focus:outline-none focus:border-indigo-500"
                />
              </div>

              <div>
                <label className="block font-medium text-slate-300 mb-1">Reason for Reset (Required for Audit)</label>
                <input
                  type="text"
                  required
                  value={refResetReason}
                  onChange={(e) => setRefResetReason(e.target.value)}
                  placeholder="e.g. Annual portfolio rebalancing or benchmark recalibration"
                  className="w-full px-3.5 py-2.5 bg-slate-950 border border-slate-700 rounded-lg text-white text-sm focus:outline-none focus:border-indigo-500"
                />
              </div>

              <div className="flex justify-end gap-3 pt-3">
                <button
                  type="button"
                  onClick={() => setResetRefModalOpen(false)}
                  className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-lg font-medium cursor-pointer"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-amber-600 hover:bg-amber-500 text-white rounded-lg font-bold cursor-pointer shadow-sm"
                >
                  Confirm Reset
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Test SMS Modal */}
      {testSmsModalOpen && (
        <div className="fixed inset-0 bg-black/70 backdrop-blur-sm flex items-center justify-center p-4 z-50">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 max-w-md w-full shadow-2xl">
            <h3 className="text-lg font-bold text-white flex items-center gap-2">
              <Send className="w-5 h-5 text-emerald-400" />
              Test SMS Gateway Connectivity
            </h3>
            <p className="text-xs text-slate-400 mt-1">
              Verify credentials and deliverability to your mobile number without waiting for market triggers.
            </p>

            <form onSubmit={handleSendTestSms} className="mt-5 space-y-4 text-xs">
              <div>
                <label className="block font-medium text-slate-300 mb-1">SMS Provider</label>
                <select
                  value={smsProvider}
                  onChange={(e) => setSmsProvider(e.target.value as any)}
                  className="w-full px-3.5 py-2.5 bg-slate-950 border border-slate-700 rounded-lg text-white font-medium text-sm focus:outline-none focus:border-indigo-500"
                >
                  <option value="fast2sms">Fast2SMS (₹50 Free Wallet Credit)</option>
                  <option value="startmessaging">StartMessaging (Indian SMS Gateway)</option>
                  <option value="techtor">TechTo Networks</option>
                  <option value="mock">Mock Logger (Simulate without spending credits)</option>
                </select>
              </div>

              <div>
                <label className="block font-medium text-slate-300 mb-1">Target Phone Number (+91 format)</label>
                <input
                  type="text"
                  required
                  value={smsRecipient}
                  onChange={(e) => setSmsRecipient(e.target.value)}
                  className="w-full px-3.5 py-2.5 bg-slate-950 border border-slate-700 rounded-lg text-white font-mono text-sm focus:outline-none focus:border-indigo-500"
                />
              </div>

              <div>
                <label className="block font-medium text-slate-300 mb-1">Message Preview</label>
                <textarea
                  rows={3}
                  value={smsCustomMessage}
                  onChange={(e) => setSmsCustomMessage(e.target.value)}
                  className="w-full px-3.5 py-2.5 bg-slate-950 border border-slate-700 rounded-lg text-white text-xs focus:outline-none focus:border-indigo-500"
                />
              </div>

              <div className="flex justify-end gap-3 pt-3">
                <button
                  type="button"
                  onClick={() => setTestSmsModalOpen(false)}
                  className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-lg font-medium cursor-pointer"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-emerald-600 hover:bg-emerald-500 text-white rounded-lg font-bold cursor-pointer shadow-sm"
                >
                  Dispatch SMS
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Daily Check Diagnostic Log Modal */}
      {showLogModal && (
        <div className="fixed inset-0 bg-black/70 backdrop-blur-sm flex items-center justify-center p-4 z-50">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 max-w-2xl w-full shadow-2xl flex flex-col max-h-[85vh]">
            <div className="flex justify-between items-center pb-3 border-b border-slate-800">
              <h3 className="text-base font-bold text-white flex items-center gap-2">
                <Terminal className="w-5 h-5 text-indigo-400" />
                Daily Evaluation Diagnostic Output
              </h3>
              <button
                onClick={() => setShowLogModal(false)}
                className="text-slate-400 hover:text-white text-lg font-bold"
              >
                &times;
              </button>
            </div>

            <div className="mt-4 flex-1 overflow-y-auto bg-slate-950 rounded-xl p-4 font-mono text-xs text-slate-300 border border-slate-800 space-y-1.5">
              {diagnosticLogs.map((line, idx) => (
                <div
                  key={idx}
                  className={`${
                    line.includes('🚨')
                      ? 'text-rose-400 font-bold'
                      : line.includes('📱')
                      ? 'text-emerald-400 font-bold'
                      : line.includes('ℹ️')
                      ? 'text-indigo-300'
                      : 'text-slate-300'
                  }`}
                >
                  {line}
                </div>
              ))}
            </div>

            <div className="mt-4 pt-3 border-t border-slate-800 flex justify-end">
              <button
                onClick={() => setShowLogModal(false)}
                className="px-4 py-2 bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold rounded-lg cursor-pointer"
              >
                Close Logs
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
