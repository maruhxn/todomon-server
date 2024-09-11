package com.maruhxn.todomon.core.global.error.exception;

import com.maruhxn.todomon.core.global.error.ErrorCode;
import org.springframework.http.HttpStatus;

public class ExistingResourceException extends TodomonException {
    public ExistingResourceException(ErrorCode code) {
        super(code, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    public ExistingResourceException(ErrorCode code, String message) {
        super(code, HttpStatus.UNPROCESSABLE_ENTITY, message);
    }
}
