package com.maruhxn.todomon.core.global.util.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;

public class AtLeastOneFieldNotNullValidator implements ConstraintValidator<AtLeastOneFieldNotNull, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return false; // 객체 자체가 null인 경우 유효하지 않음
        }

        try {
            for (Field field : value.getClass().getDeclaredFields()) {
                field.setAccessible(true); // private 필드 접근 가능
                if (field.get(value) != null) {
                    return true; // 하나라도 null이 아니면 유효
                }
            }
        } catch (IllegalAccessException e) {
            return false;
        }

        return false; // 모든 필드가 null인 경우 유효하지 않음
    }
}
