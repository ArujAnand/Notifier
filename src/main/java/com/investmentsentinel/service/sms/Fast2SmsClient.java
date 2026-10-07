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
 * Fast2SMS Client (Leading Indian SMS Gateway with verifiable ₹50 instant free trial balance).
 * Supports both Quick testing route ('q') and official DLT route ('dlt').
 */
@Component
public class Fast2SmsClient implements SmsProviderClient {

    private static final Logger log = LoggerFactory.getLogger(Fast2SmsClient.class);
    private static final String FAST2SMS_URL = "https://www.fast2sms.com/dev/bulkV2";

    private final RestClient restClient;
    private final AppProperties properties;

    public Fast2SmsClient(RestClient restClient, AppProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    @Override
    public String getProviderName() {
        return "fast2sms";
    }

    @Override
    public SmsResponse sendSms(SmsRequest request) {
        String apiKey = properties.getSms().getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            return SmsResponse.failure(getProviderName(), "Missing SMS_API_KEY", null);
        }

        String cleanPhone = request.recipient().replaceAll("[^0-9]", "");
        if (cleanPhone.length() > 10 && cleanPhone.startsWith("91")) {
            cleanPhone = cleanPhone.substring(2);
        }

        String route = (properties.getSms().getDltTemplateId() != null && !properties.getSms().getDltTemplateId().isBlank())
                ? "dlt" : "q";

        Map<String, Object> payload;
        if ("dlt".equals(route)) {
            payload = Map.of(
                    "route", "dlt",
                    "sender_id", properties.getSms().getSenderId(),
                    "message", properties.getSms().getDltTemplateId(),
                    "variables_values", request.message(),
                    "numbers", cleanPhone
            );
        } else {
            // Quick / Developer testing route
            payload = Map.of(
                    "route", "q",
                    "message", request.message(),
                    "language", "english",
                    "numbers", cleanPhone
            );
        }

        try {
            log.info("Dispatching SMS via Fast2SMS using route '{}'...", route);

            JsonNode response = restClient.post()
                    .uri(FAST2SMS_URL)
                    .header("authorization", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(JsonNode.class);

            if (response != null && response.path("return").asBoolean(false)) {
                String reqId = response.path("request_id").asText(UUID.randomUUID().toString());
                return SmsResponse.success(getProviderName(), reqId, response.toString());
            } else {
                String message = response != null ? response.path("message").asText("Unknown Fast2SMS failure") : "Null response";
                log.warn("Fast2SMS error: {}", message);
                return SmsResponse.failure(getProviderName(), message, response != null ? response.toString() : null);
            }
        } catch (Exception e) {
            log.error("Failed to send SMS via Fast2SMS: {}", e.getMessage());
            return SmsResponse.failure(getProviderName(), e.getMessage(), null);
        }
    }
}
