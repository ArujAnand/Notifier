# Indian SMS API Providers & DLT Compliance Guide 📱

This guide documents verified SMS options for sending transactional investment reminders to Indian phone numbers (`+91XXXXXXXXXX`), how to obtain free trial credits, and how to comply with Telecom Regulatory Authority of India (TRAI) DLT requirements.

---

## 1. Provider Comparison & Free Credit Verification

| Provider | Free Trial / Testing Credits | API Difficulty | Indian DLT Requirement for Testing | Notes |
| :--- | :--- | :--- | :--- | :--- |
| **Fast2SMS** (`fast2sms`) | **₹50 Free Wallet Balance** upon mobile verification | Minimal (REST JSON) | **Optional** for Quick route (`route: "q"`), Required for custom sender ID | **Recommended for instant start**: Gives ₹50 test balance immediately to test to your own verified mobile number. |
| **StartMessaging** (`startmessaging`) | Trial balance provided on account activation | Low (REST JSON) | Required for registered Sender IDs | Indian enterprise gateway supporting REST webhook and HTTP endpoints. |
| **TechTo Networks** (`techtor`) | Trial testing credits on registration | Low (HTTP GET/POST) | Required for promotional/commercial headers | Established Indian SMS gateway. |
| **Twilio (India)** (`twilio`) | $15.50 trial credits | Moderate | Requires registered Sender ID or Alpha sender | Can be configured easily via custom client. |
| **Mock Provider** (`mock`) | Unlimited (Local simulation) | None | None | Logs formatted SMS safely with masked phone numbers. |

---

## 2. Fast2SMS Setup (Recommended for Immediate Free Testing)

Fast2SMS offers an instant developer wallet with ₹50 free testing balance without requiring business registration upfront.

### How to get the Free Trial:
1. Go to [https://www.fast2sms.com](https://www.fast2sms.com) and sign up with your Indian mobile number (`+91XXXXXXXXXX`).
2. Verify your mobile number with OTP. Your account will be credited with **₹50 testing balance**.
3. Go to **Dev API** in the left sidebar: [https://www.fast2sms.com/dashboard/dev-api](https://www.fast2sms.com/dashboard/dev-api).
4. Copy your **API Authorization Key**.
5. Set your environment variables:
   ```env
   SMS_PROVIDER=fast2sms
   SMS_API_KEY=your_fast2sms_api_key_here
   SMS_RECIPIENT=+919876543210
   SMS_SENDER_ID=FSTSMS
   ```
6. The `Fast2SmsClient` will automatically use the Quick testing route (`q`) if no DLT template ID is supplied, allowing immediate delivery of reminders.

---

## 3. Understanding Indian Telecom (TRAI) DLT Compliance

Under TRAI's Distributed Ledger Technology (DLT) regulations:
- Commercial and bulk SMS sent over Indian telecom networks must route through registered telecom operators (Jio, Airtel, Vodafone Idea, BSNL, MTNL).
- Compliance steps for long-term production:
  1. **Entity Registration**: Register your business / individual profile on any telco portal (e.g. Jio DLT / Airtel DLT / Vilpower).
  2. **Sender ID (Header)**: Register a 6-character alphabetic header representing your brand (e.g. `INVSTL`, `STMSNG`).
  3. **Content Template**: Submit pre-approved SMS templates with variables `{#var#}`.

### Example Compliant DLT Template for Scheduled Review:
```
{#var#} review due. Last: {#var#} on {#var#}. Reference: {#var#}, Current: {#var#}, Drawdown: {#var#}%. Suggested: {#var#}. No trade was placed.
```

### Example Compliant DLT Template for Threshold Alert:
```
{#var#} alert: {#var#} crossed {#var#}% from reference. Current drawdown: {#var#}%. Next review: {#var#} days. No trade was placed.
```

When you have a registered DLT template ID:
```env
SMS_DLT_TEMPLATE_ID=1207161234567890123
SMS_SENDER_ID=INVSTL
```
The application will automatically inject the DLT template ID into payload headers.

---

## 4. Privacy & Masking Policy

Investment Sentinel implements strict masking across all logs:
- Recipient numbers are masked in console and actuator logs as `+91******3210`.
- API keys are never printed in log outputs.
- Database entries in `notifications` record status and message ID without exposing raw credentials.
