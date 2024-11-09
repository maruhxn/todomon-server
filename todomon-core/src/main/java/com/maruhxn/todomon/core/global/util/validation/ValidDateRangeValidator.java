package com.maruhxn.todomon.core.global.util.validation;


import com.maruhxn.todomon.core.domain.todo.dto.request.DateRangeDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.Duration;
import java.time.LocalDate;

public class ValidDateRangeValidator implements ConstraintValidator<ValidDateRange, DateRangeDto> {

    private String startAtIsBeforeThanTodayMessage;
    private String startAtAfterEndAtMessage;
    private String overOneDayMessage;

    @Override
    public void initialize(ValidDateRange constraintAnnotation) {
        this.startAtIsBeforeThanTodayMessage = constraintAnnotation.startAtIsBeforeThanTodayMessage();
        this.startAtAfterEndAtMessage = constraintAnnotation.startAtAfterEndAtMessage();
        this.overOneDayMessage = constraintAnnotation.overOneDayMessage();
    }

    @Override
    public boolean isValid(DateRangeDto dto, ConstraintValidatorContext context) {
        if (dto.getStartAt() == null || dto.getEndAt() == null) {
            return true; // 다른 검증기에 의해 처리됨
        }

        boolean startAtIsBeforeThanToday = dto.getStartAt().toLocalDate().isBefore(LocalDate.now());
        if (startAtIsBeforeThanToday) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(startAtIsBeforeThanTodayMessage)
                    .addPropertyNode("startAt")
                    .addConstraintViolation();
            return false;
        }

        boolean startAtIsAfterThanEndAt = dto.getStartAt().isAfter(dto.getEndAt());
        if (startAtIsAfterThanEndAt) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(startAtAfterEndAtMessage)
                    .addPropertyNode("endAt")
                    .addConstraintViolation();
            return false;
        }

        Duration duration = Duration.between(dto.getStartAt(), dto.getEndAt());
        boolean isOverOneDay = duration.toDays() > 1;

        if (isOverOneDay) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(overOneDayMessage)
                    .addPropertyNode("endAt")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}