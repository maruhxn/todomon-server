package com.maruhxn.todomon.core.global.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

@Getter
@RequiredArgsConstructor
public enum PermitAllUrls {

    MAIN("/", GET),
    REFRESH("/api/auth/refresh", GET),
    GET_ALL_PETS("/api/pets", GET),
    PAYMENT_COMPLETE("/api/payment/complete", POST),
    ACTUATOR("/actuator/**", GET);

    private final String url;
    private final HttpMethod method;
}
