package com.maruhxn.todomon.core.domain.todo.dto.request;

import java.time.LocalDateTime;

public interface DateRangeDto {
    LocalDateTime getStartAt();
    LocalDateTime getEndAt();
}
