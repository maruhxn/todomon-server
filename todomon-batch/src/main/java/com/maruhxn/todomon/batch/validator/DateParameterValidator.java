package com.maruhxn.todomon.batch.validator;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.JobParametersValidator;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class DateParameterValidator implements JobParametersValidator {

    private final String DATE = "date";

    @Override
    public void validate(JobParameters parameters) throws JobParametersInvalidException {
        String date = parameters.getString(DATE);

        if (!StringUtils.hasText(date)) {
            throw new JobParametersInvalidException(DATE + "가 빈 문자열이거나 존재하지 않습니다.");
        }

        try {
            LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            throw new JobParametersInvalidException(DATE + "가 올바른 날짜 형식이 아닙니다. yyyy-MM-dd 형식이어야 합니다.");
        }
    }
}
