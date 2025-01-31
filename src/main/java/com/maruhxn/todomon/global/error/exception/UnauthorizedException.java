package com.maruhxn.todomon.global.error.exception;

import com.maruhxn.todomon.global.error.ErrorCode;
import org.springframework.http.HttpStatus;

public class UnauthorizedException extends TodomonException {
    public UnauthorizedException(ErrorCode code) {
        super(code, HttpStatus.UNAUTHORIZED);
    }

    public UnauthorizedException(ErrorCode code, String message) {
        super(code, HttpStatus.UNAUTHORIZED, message);
    }
}
