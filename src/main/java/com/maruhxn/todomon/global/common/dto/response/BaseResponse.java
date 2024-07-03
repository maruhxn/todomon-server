package com.maruhxn.todomon.global.common.dto.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BaseResponse {
    private String code;
    private String message;

    public BaseResponse(String message) {
        this.code = "OK";
        this.message = message;
    }

    public BaseResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }

}