package com.investmentsentinel.service.sms;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.investmentsentinel.config.AppProperties;

/**
 * SMS Provider Client for TechTo Networks (Indian SMS Gateway with trial support).
 */
@Component
public class TechToNetworksClient implements SmsProviderClient {

    private static final Logger log = LoggerFactory.getLogger(TechToNetworksClient.class);
    private static final String DEFAULT_URL = "https://api.techtor.com/api/v2/sms/send";

    private final RestClient restClient;
    private final AppProperties properties;

    public TechToNetworksClient(RestClient restClient, AppProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    @Override
    public String getProviderName() {
        return "techtor";
    }

    @Override
    public SmsResponse sendSms(SmsRequest request) {
        String apiKey = properties.getSms().getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            return SmsResponse.failure(getProviderName(), "Missing SMS_API_KEY", null);
        }

        String cleanPhone = request.recipient().replaceAll("[^0-9]", "");

        try {
            String uri = String.format("%s?apiKey=%s&sender=%s&to=%s&message=%s",
                    DEFAULT_URL,
                    URLEncoder.encode(apiKey, StandardCharsets.UTF_8),
                    URLEncoder.encode(properties.getSms().getSenderId(), StandardCharsets.UTF_8),
                    URLEncoder.encode(cleanPhone, StandardCharsets.UTF_8),
                    URLEncoder.encode(request.message(), StandardCharsets.UTF_8));

            log.info("Dispatching SMS via TechTo Networks...");
            String rawResponse = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(String.class);

            if (rawResponse != null && (rawResponse.contains("success") || rawResponse.contains("200") || rawResponse.contains("SUBMITTED"))) {
                return SmsResponse.success(getProviderName(), UUID.randomUUID().toString(), rawResponse);
            } else {
                return SmsResponse.failure(getProviderName(), "Provider rejected request: " + rawResponse, rawResponse);
            }
        } catch (Exception e) {
            log.error("Failed to send SMS via TechTo Networks: {}", e.getMessage());
            return SmsResponse.failure(getProviderName(), e.getMessage(), null);
        }
    }
}
