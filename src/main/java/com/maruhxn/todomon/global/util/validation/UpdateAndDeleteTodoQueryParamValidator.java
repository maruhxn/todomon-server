package com.maruhxn.todomon.global.util.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UpdateAndDeleteTodoQueryParamValidator implements ConstraintValidator<ValidUpdateAndDeleteTodoQueryParams, com.maruhxn.todomon.domain.todo.dto.request.UpdateAndDeleteTodoQueryParams> {

    @Override
    public boolean isValid(com.maruhxn.todomon.domain.todo.dto.request.UpdateAndDeleteTodoQueryParams queryParams, ConstraintValidatorContext context) {
        if (Boolean.TRUE.equals(queryParams.getIsInstance()) && queryParams.getTargetType() == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("수정 적용 범위를 설정해주세요.")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}