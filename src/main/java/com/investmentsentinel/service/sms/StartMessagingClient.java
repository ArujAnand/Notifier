package com.investmentsentinel.service.sms;

import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.investmentsentinel.config.AppProperties;

/**
 * SMS Provider Client for StartMessaging (Indian SMS API gateway with trial balance).
 * Fully respects Indian DLT template guidelines.
 */
@Component
public class StartMessagingClient implements SmsProviderClient {

    private static final Logger log = LoggerFactory.getLogger(StartMessagingClient.class);
    private static final String DEFAULT_ENDPOINT = "https://api.startmessaging.com/v1/sms/send";

    private final RestClient restClient;
    private final AppProperties properties;

    public StartMessagingClient(RestClient restClient, AppProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    @Override
    public String getProviderName() {
        return "startmessaging";
    }

    @Override
    public SmsResponse sendSms(SmsRequest request) {
        String apiKey = properties.getSms().getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            log.error("StartMessaging API Key is not configured in SMS_API_KEY");
            return SmsResponse.failure(getProviderName(), "Missing SMS_API_KEY", null);
        }

        // Clean recipient number for Indian format (10 digits without +91 or with 91)
        String cleanPhone = request.recipient().replaceAll("[^0-9]", "");
        if (cleanPhone.length() > 10 && cleanPhone.startsWith("91")) {
            cleanPhone = cleanPhone.substring(2);
        }

        Map<String, Object> payload = Map.of(
                "apiKey", apiKey,
                "sender", request.senderId() != null ? request.senderId() : properties.getSms().getSenderId(),
                "mobile", cleanPhone,
                "message", request.message(),
                "templateId", request.templateId() != null ? request.templateId() : properties.getSms().getDltTemplateId()
        );

        try {
            log.info("Dispatching SMS via StartMessaging to recipient (length: {} chars)", request.message().length());

            JsonNode response = restClient.post()
                    .uri(DEFAULT_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(JsonNode.class);

            if (response != null && (response.path("status").asText().equalsIgnoreCase("success") ||
                    response.path("code").asInt() == 200 || response.path("success").asBoolean())) {
                String messageId = response.path("messageId").asText(UUID.randomUUID().toString());
                return SmsResponse.success(getProviderName(), messageId, response.toString());
            } else {
                String errorMsg = response != null ? response.path("message").asText("Unknown provider error") : "No response body";
                log.warn("StartMessaging returned error: {}", errorMsg);
                return SmsResponse.failure(getProviderName(), errorMsg, response != null ? response.toString() : null);
            }
        } catch (Exception e) {
            log.error("HTTP error sending SMS via StartMessaging: {}", e.getMessage());
            return SmsResponse.failure(getProviderName(), e.getMessage(), null);
        }
    }
}
