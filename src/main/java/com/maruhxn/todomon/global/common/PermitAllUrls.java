package com.maruhxn.todomon.global.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;

import static org.springframework.http.HttpMethod.GET;

@Getter
@RequiredArgsConstructor
public enum PermitAllUrls {

    REFRESH("/api/auth/refresh", GET);

    private final String url;
    private final HttpMethod method;
}
