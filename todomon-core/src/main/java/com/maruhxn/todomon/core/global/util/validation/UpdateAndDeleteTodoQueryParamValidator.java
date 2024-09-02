package com.maruhxn.todomon.core.global.util.validation;

import com.maruhxn.todomon.core.domain.todo.dto.request.UpdateAndDeleteTodoQueryParams;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UpdateAndDeleteTodoQueryParamValidator implements ConstraintValidator<ValidUpdateAndDeleteTodoQueryParams, UpdateAndDeleteTodoQueryParams> {

    @Override
    public boolean isValid(UpdateAndDeleteTodoQueryParams queryParams, ConstraintValidatorContext context) {
        if (Boolean.TRUE.equals(queryParams.getIsInstance()) && queryParams.getTargetType() == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("수정 적용 범위를 설정해주세요.")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}