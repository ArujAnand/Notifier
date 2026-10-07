package com.investmentsentinel.service.sms;

public record SmsRequest(
        String recipient,
        String message,
        String senderId,
        String templateId
) {}
