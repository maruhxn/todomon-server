package com.maruhxn.todomon.global.util.validation;


import com.maruhxn.todomon.domain.todo.dto.request.CreateTodoReq;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidDateRangeValidator implements ConstraintValidator<ValidDateRange, CreateTodoReq> {

    @Override
    public void initialize(ValidDateRange constraintAnnotation) {
        // 초기화 코드 (필요한 경우)
    }

    @Override
    public boolean isValid(CreateTodoReq request, ConstraintValidatorContext context) {
        if (request.getStartAt() == null || request.getEndAt() == null) {
            return true; // 다른 검증기에 의해 처리됨
        }
        boolean isValid = request.getStartAt().isBefore(request.getEndAt());
        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("endAt")
                    .addConstraintViolation();
        }
        return isValid;
    }
}