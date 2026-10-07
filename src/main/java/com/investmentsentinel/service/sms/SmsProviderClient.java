package com.investmentsentinel.service.sms;

public interface SmsProviderClient {

    /**
     * Provider identification key (e.g. 'startmessaging', 'techtor', 'fast2sms', 'mock').
     */
    String getProviderName();

    /**
     * Dispatches an SMS payload to the external provider.
     */
    SmsResponse sendSms(SmsRequest request);
}
