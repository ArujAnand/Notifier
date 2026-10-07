package com.investmentsentinel;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mockito;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.investmentsentinel.config.AppProperties;
import com.investmentsentinel.domain.NotificationRecord;
import com.investmentsentinel.repository.NotificationRepository;
import com.investmentsentinel.service.sms.LoggingMockSmsClient;
import com.investmentsentinel.service.sms.SmsProviderClient;
import com.investmentsentinel.service.sms.SmsResponse;
import com.investmentsentinel.service.sms.SmsServiceImpl;

class SmsServiceTest {

    private SmsProviderClient mockProvider;
    private NotificationRepository notificationRepository;
    private AppProperties properties;
    private SmsServiceImpl smsService;

    @BeforeEach
    void setUp() {
        mockProvider = Mockito.mock(SmsProviderClient.class);
        when(mockProvider.getProviderName()).thenReturn("fast2sms");

        notificationRepository = Mockito.mock(NotificationRepository.class);

        properties = new AppProperties();
        properties.getSms().setProvider("fast2sms");
        properties.getSms().setMaxRetries(3);
        properties.getSms().setRetryDelayMs(10); // fast for tests
        properties.getSms().setRecipient("+919876543210");

        smsService = new SmsServiceImpl(List.of(mockProvider), notificationRepository, properties);
    }

    @Test
    @DisplayName("SMS succeeds on first attempt and records 'SENT' in database")
    void testSmsSuccessFirstAttempt() {
        when(mockProvider.sendSms(any())).thenReturn(SmsResponse.success("fast2sms", "MSG-101", "{\"ok\":true}"));

        SmsResponse response = smsService.sendNotification(1L, "SYSTEM_TEST", BigDecimal.ZERO, "Test message");

        assertThat(response.success()).isTrue();
        verify(mockProvider, times(1)).sendSms(any());
        verify(notificationRepository).save(Mockito.argThat(rec -> "SENT".equals(rec.getStatus())));
    }

    @Test
    @DisplayName("SMS retries up to 3 times on transient failures before succeeding")
    void testSmsRetriesOnFailure() {
        when(mockProvider.sendSms(any()))
                .thenReturn(SmsResponse.failure("fast2sms", "Network timeout", null))
                .thenReturn(SmsResponse.failure("fast2sms", "503 Gateway busy", null))
                .thenReturn(SmsResponse.success("fast2sms", "MSG-102", "{\"ok\":true}"));

        SmsResponse response = smsService.sendNotification(1L, "SYSTEM_TEST", BigDecimal.ZERO, "Test retry message");

        assertThat(response.success()).isTrue();
        verify(mockProvider, times(3)).sendSms(any());
        verify(notificationRepository).save(Mockito.argThat(rec -> "SENT".equals(rec.getStatus())));
    }

    @Test
    @DisplayName("SMS logs 'FAILED' status when all retries are exhausted")
    void testSmsFailureExhausted() {
        when(mockProvider.sendSms(any()))
                .thenReturn(SmsResponse.failure("fast2sms", "Authentication error", null));

        SmsResponse response = smsService.sendNotification(1L, "SYSTEM_TEST", BigDecimal.ZERO, "Failing message");

        assertThat(response.success()).isFalse();
        verify(mockProvider, times(3)).sendSms(any());
        verify(notificationRepository).save(Mockito.argThat(rec -> "FAILED".equals(rec.getStatus())));
    }

    @Test
    @DisplayName("Phone numbers are masked to protect user privacy in logs")
    void testPhoneNumberMasking() {
        String original = "+919876543210";
        String masked = LoggingMockSmsClient.maskPhoneNumber(original);

        assertThat(masked).isEqualTo("+91******3210");
        assertThat(masked).doesNotContain("987654");
    }
}
