package com.maruhxn.todomon.global.error.exception;

import com.maruhxn.todomon.global.error.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class TodomonException extends RuntimeException {
    private final ErrorCode code;
    private final HttpStatus httpStatus;

    protected TodomonException(ErrorCode code, HttpStatus httpStatus) {
        super(code.getMessage());
        this.code = code;
        this.httpStatus = httpStatus;
    }

    protected TodomonException(ErrorCode code, HttpStatus httpStatus, String message) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }
}
