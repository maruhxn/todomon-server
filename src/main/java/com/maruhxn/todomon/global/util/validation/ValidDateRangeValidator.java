package com.maruhxn.todomon.global.util.validation;


import com.maruhxn.todomon.domain.todo.dto.request.DateRangeDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidDateRangeValidator implements ConstraintValidator<ValidDateRange, DateRangeDto> {

    @Override
    public void initialize(ValidDateRange constraintAnnotation) {
        // 초기화 코드 (필요한 경우)
    }

    @Override
    public boolean isValid(DateRangeDto dto, ConstraintValidatorContext context) {
        if (dto.getStartAt() == null || dto.getEndAt() == null) {
            return true; // 다른 검증기에 의해 처리됨
        }
        boolean isValid = dto.getStartAt().isBefore(dto.getEndAt());
        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("endAt")
                    .addConstraintViolation();
        }
        return isValid;
    }
}