package com.maruhxn.todomon.global.error.exception;

import com.maruhxn.todomon.global.error.ErrorCode;
import org.springframework.http.HttpStatus;

public class InternalServerException extends TodomonException {
    public InternalServerException(ErrorCode code) {
        super(code, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public InternalServerException(ErrorCode code, String message) {
        super(code, HttpStatus.INTERNAL_SERVER_ERROR, message);
    }
}
