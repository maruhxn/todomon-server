package com.maruhxn.todomon.infra.payment.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class IamportResponse<T> {
    int code;
    String message;
    T response;

    public IamportResponse(int code, String message, T response) {
        this.code = code;
        this.message = message;
        this.response = response;
    }
}