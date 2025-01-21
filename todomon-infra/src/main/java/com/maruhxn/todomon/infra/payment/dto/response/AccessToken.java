package com.maruhxn.todomon.infra.payment.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AccessToken {
    @JsonProperty("access_token")
    String access_token;
    @JsonProperty("expired_at")
    long expired_at;
    @JsonProperty("now")
    long now;

    public AccessToken(String access_token, long expired_at, long now) {
        this.access_token = access_token;
        this.expired_at = expired_at;
        this.now = now;
    }
}