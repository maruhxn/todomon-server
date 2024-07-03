package com.maruhxn.todomon.global.common.dto.response;

import lombok.Getter;

@Getter
public class DataResponse<T> extends BaseResponse {
    private final T data;

    protected DataResponse(String code, String message, T data) {
        super(code, message);
        this.data = data;
    }

    public static <T> DataResponse<T> of(String message, T data) {
        return new DataResponse<>("OK", message, data);
    }
}