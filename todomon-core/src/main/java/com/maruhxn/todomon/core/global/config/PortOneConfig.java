package com.maruhxn.todomon.core.global.config;

import com.maruhxn.todomon.infra.payment.IamportProvider;
import com.maruhxn.todomon.infra.payment.PaymentProvider;
import com.maruhxn.todomon.infra.payment.client.IamportClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PortOneConfig {

    @Value("${portone.apiKey}")
    private String apiKey;

    @Value("${portone.secretKey}")
    private String secretKey;

    @Bean
    public IamportClient iamportClient() {
        return new IamportClient(apiKey, secretKey);
    }

    @Bean
    public PaymentProvider paymentProvider() {
        return new IamportProvider(iamportClient());
    }

}
