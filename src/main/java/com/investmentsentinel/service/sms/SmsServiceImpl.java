package com.investmentsentinel.service.sms;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.investmentsentinel.config.AppProperties;
import com.investmentsentinel.domain.NotificationRecord;
import com.investmentsentinel.repository.NotificationRepository;

@Service
public class SmsServiceImpl implements SmsService {

    private static final Logger log = LoggerFactory.getLogger(SmsServiceImpl.class);

    private final Map<String, SmsProviderClient> providers;
    private final NotificationRepository notificationRepository;
    private final AppProperties properties;

    public SmsServiceImpl(List<SmsProviderClient> providerClients,
                          NotificationRepository notificationRepository,
                          AppProperties properties) {
        this.providers = providerClients.stream()
                .collect(Collectors.toMap(client -> client.getProviderName().toLowerCase(), Function.identity()));
        this.notificationRepository = notificationRepository;
        this.properties = properties;
    }

    @Override
    @Transactional
    public SmsResponse sendNotification(Long investmentId, String notificationType, BigDecimal threshold, String message) {
        SmsResponse response = dispatchWithRetry(message);

        // Record in database
        NotificationRecord record = new NotificationRecord(
                investmentId,
                notificationType,
                threshold,
                message,
                response.providerName(),
                response.messageId(),
                response.success() ? "SENT" : "FAILED"
        );
        notificationRepository.save(record);

        return response;
    }

    @Override
    @Transactional
    public SmsResponse sendTestSms(String testMessage) {
        String msg = (testMessage != null && !testMessage.isBlank())
                ? testMessage
                : "Investment Sentinel Test: SMS delivery verified successfully at " + java.time.LocalDateTime.now() + ". No trade was placed.";
        return sendNotification(null, "SYSTEM_TEST", null, msg);
    }

    private SmsResponse dispatchWithRetry(String message) {
        String configuredProvider = properties.getSms().getProvider().toLowerCase();
        SmsProviderClient client = providers.get(configuredProvider);

        if (client == null) {
            log.warn("Configured SMS provider '{}' not found. Falling back to mock logger.", configuredProvider);
            client = providers.getOrDefault("mock", new LoggingMockSmsClient());
        }

        String recipient = properties.getSms().getRecipient();
        SmsRequest request = new SmsRequest(
                recipient,
                message,
                properties.getSms().getSenderId(),
                properties.getSms().getDltTemplateId()
        );

        int maxRetries = properties.getSms().getMaxRetries();
        long retryDelayMs = properties.getSms().getRetryDelayMs();

        SmsResponse lastResponse = null;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            log.info("Sending SMS attempt {}/{} via [{}] to recipient {}",
                    attempt, maxRetries, client.getProviderName(), LoggingMockSmsClient.maskPhoneNumber(recipient));

            lastResponse = client.sendSms(request);
            if (lastResponse.success()) {
                log.info("SMS delivered successfully. Provider Message ID: {}", lastResponse.messageId());
                return lastResponse;
            }

            log.warn("SMS attempt {} failed: {}", attempt, lastResponse.errorMessage());
            if (attempt < maxRetries) {
                try {
                    Thread.sleep(retryDelayMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        return lastResponse != null
                ? lastResponse
                : SmsResponse.failure(client.getProviderName(), "Exhausted all retries", null);
    }
}
