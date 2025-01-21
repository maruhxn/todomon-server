package com.maruhxn.todomon.core.global.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;

import static org.springframework.http.HttpMethod.GET;

@Getter
@RequiredArgsConstructor
public enum PermitAllUrls {

    REFRESH("/api/auth/refresh", GET),
    GET_ALL_PETS("/api/pets", GET),
    ACTUATOR("/actuator/**", GET);

    private final String url;
    private final HttpMethod method;
}
