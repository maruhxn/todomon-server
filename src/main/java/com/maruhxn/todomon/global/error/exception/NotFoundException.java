package com.maruhxn.todomon.global.error.exception;

import com.maruhxn.todomon.global.error.ErrorCode;
import org.springframework.http.HttpStatus;

public class NotFoundException extends TodomonException {
    public NotFoundException(ErrorCode code) {
        super(code, HttpStatus.NOT_FOUND);
    }

    public NotFoundException(ErrorCode code, String message) {
        super(code, HttpStatus.NOT_FOUND, message);
    }
}
