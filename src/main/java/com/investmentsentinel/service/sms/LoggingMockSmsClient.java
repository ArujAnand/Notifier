package com.investmentsentinel.service.sms;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Mock SMS Provider Client for local development, demo mode, and offline operation.
 * Logs the simulated SMS safely with masked phone numbers.
 */
@Component
public class LoggingMockSmsClient implements SmsProviderClient {

    private static final Logger log = LoggerFactory.getLogger(LoggingMockSmsClient.class);

    @Override
    public String getProviderName() {
        return "mock";
    }

    @Override
    public SmsResponse sendSms(SmsRequest request) {
        String maskedRecipient = maskPhoneNumber(request.recipient());
        log.info("""
                ====================== [SIMULATED SMS DISPATCH] ======================
                To: {} | Sender: {}
                Message:
                {}
                ======================================================================
                """, maskedRecipient, request.senderId(), request.message());

        return SmsResponse.success(getProviderName(), "MOCK-" + UUID.randomUUID(), "{\"status\":\"simulated_ok\"}");
    }

    public static String maskPhoneNumber(String phone) {
        if (phone == null || phone.length() < 6) return "******";
        int len = phone.length();
        return phone.substring(0, 3) + "*".repeat(Math.max(3, len - 7)) + phone.substring(len - 4);
    }
}
