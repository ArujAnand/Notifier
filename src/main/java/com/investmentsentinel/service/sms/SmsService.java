package com.investmentsentinel.service.sms;

import java.math.BigDecimal;

public interface SmsService {

    /**
     * Sends an investment notification SMS and persists audit record in notifications table.
     */
    SmsResponse sendNotification(Long investmentId, String notificationType, BigDecimal threshold, String message);

    /**
     * Sends a direct test SMS for verifying provider credentials and deliverability.
     */
    SmsResponse sendTestSms(String testMessage);
}
