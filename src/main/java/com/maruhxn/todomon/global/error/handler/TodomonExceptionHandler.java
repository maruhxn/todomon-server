package com.maruhxn.todomon.global.error.handler;

import com.maruhxn.todomon.global.common.dto.response.ErrorResponse;
import com.maruhxn.todomon.global.error.ErrorCode;
import com.maruhxn.todomon.global.error.exception.TodomonException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@RestControllerAdvice
public class TodomonExceptionHandler {

    // 404 예외 처리 핸들러
    @ExceptionHandler
    public ResponseEntity<Object> handle404(NoHandlerFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(ErrorCode.NOT_FOUND_RESOURCE));
    }

    // 인가 예외 처리 핸들러
    @ExceptionHandler
    public ResponseEntity<Object> accessDenied(AccessDeniedException e) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.of(ErrorCode.FORBIDDEN));
    }

    // DB 예외 처리 핸들러
    @ExceptionHandler
    public ResponseEntity<Object> dataIntegrityViolation(DataIntegrityViolationException e) {
        log.error(e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ErrorResponse.of(ErrorCode.EXISTING_RESOURCE));
    }

    // 제약조건 예외 처리 핸들러
    @ExceptionHandler
    public ResponseEntity<Object> constraintViolation(ConstraintViolationException e) {
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ErrorResponse.of(ErrorCode.EXISTING_RESOURCE, e.getMessage()));
    }

    // DTO 유효성 검증 예외 처리
    @ExceptionHandler
    public ResponseEntity<Object> validationFail(MethodArgumentNotValidException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.validationError(e.getBindingResult()));
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<Object> pathVariableValidationFail(Exception e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(ErrorCode.PATH_ERROR));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<Object> missingHeader(MissingRequestHeaderException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(ErrorCode.EMPTY_REFRESH_TOKEN));
    }

    @ExceptionHandler
    public ResponseEntity<Object> todomonException(TodomonException e) {
        return ResponseEntity
                .status(e.getHttpStatus())
                .body(ErrorResponse.of(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<Object> exception(Exception e) {
        log.error(e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(ErrorCode.INTERNAL_ERROR, e.getMessage()));
    }
}
