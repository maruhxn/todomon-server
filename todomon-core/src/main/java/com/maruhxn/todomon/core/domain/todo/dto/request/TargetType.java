package com.maruhxn.todomon.core.domain.todo.dto.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TargetType {
    THIS_TASK("이 할 일"),
    //    FUTURE_TASKS("이번 및 향후 할 일"),
    ALL_TASKS("모든 할 일");

    private final String description;
}
