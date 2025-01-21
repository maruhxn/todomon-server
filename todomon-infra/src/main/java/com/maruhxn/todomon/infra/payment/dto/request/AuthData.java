package com.maruhxn.todomon.infra.payment.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AuthData {
    private String imp_key;
    private String imp_secret;

    public AuthData(String imp_key, String imp_secret) {
        this.imp_key = imp_key;
        this.imp_secret = imp_secret;
    }
}
