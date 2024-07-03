package com.maruhxn.todomon.global.common.dto;

import lombok.Getter;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class FieldError {

    private String field;
    private String value;
    private String reason;

    private FieldError(final String field, final String value, final String reason) {
        this.field = field;
        this.value = value;
        this.reason = reason;
    }

    public static List<FieldError> from(final BindingResult bindingResult) {
        final List<org.springframework.validation.FieldError> fieldErrors = bindingResult.getFieldErrors();
        return fieldErrors.stream()
                .map(error -> new FieldError(
                        error.getField(),
                        error.getRejectedValue() == null ? "" : error.getRejectedValue().toString(),
                        error.getDefaultMessage()))
                .collect(Collectors.toList());
    }
}