package com.maruhxn.todomon.domain.todo.dto.request;

import java.time.LocalDateTime;

public interface DateRangeDto {
    LocalDateTime getStartAt();
    LocalDateTime getEndAt();
}
