package com.maruhxn.todomon.core.global.util.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UpdateAndDeleteTodoQueryParamValidator.class)
public @interface ValidUpdateAndDeleteTodoQueryParams {
    String message() default "Invalid query parameters";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}