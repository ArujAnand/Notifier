package com.investmentsentinel.service.sms;

public record SmsResponse(
        boolean success,
        String providerName,
        String messageId,
        String errorMessage,
        String rawResponse
) {
    public static SmsResponse success(String providerName, String messageId, String rawResponse) {
        return new SmsResponse(true, providerName, messageId, null, rawResponse);
    }

    public static SmsResponse failure(String providerName, String errorMessage, String rawResponse) {
        return new SmsResponse(false, providerName, null, errorMessage, rawResponse);
    }
}
