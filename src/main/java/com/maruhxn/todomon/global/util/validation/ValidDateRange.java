package com.maruhxn.todomon.global.util.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ValidDateRangeValidator.class) // 검증기 지정
@Target({ElementType.TYPE}) // 어노테이션 지정 대상 -> 여기서는 클래스
@Retention(RetentionPolicy.RUNTIME) // 애노테이션이 유지되는 기간 -> RUNTIME은 런타임 동안 유지
public @interface ValidDateRange {
    String message() default "종료 시간은 시작 시간보다 이후여야 합니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}