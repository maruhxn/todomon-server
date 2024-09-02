package com.maruhxn.todomon.core.global.error.exception;

import com.maruhxn.todomon.core.global.error.ErrorCode;
import org.springframework.http.HttpStatus;

public class BadRequestException extends TodomonException {
    public BadRequestException(ErrorCode code) {
        super(code, HttpStatus.BAD_REQUEST);
    }

    public BadRequestException(ErrorCode code, String message) {
        super(code, HttpStatus.BAD_REQUEST, message);
    }
}
