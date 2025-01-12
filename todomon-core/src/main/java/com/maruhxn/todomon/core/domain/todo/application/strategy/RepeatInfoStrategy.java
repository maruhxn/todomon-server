package com.maruhxn.todomon.core.domain.todo.application.strategy;

import com.maruhxn.todomon.core.domain.todo.domain.Frequency;
import com.maruhxn.todomon.core.domain.todo.domain.RepeatInfo;
import com.maruhxn.todomon.core.domain.todo.domain.Todo;
import com.maruhxn.todomon.core.domain.todo.domain.TodoInstance;

import java.time.LocalDateTime;
import java.util.List;

public interface RepeatInfoStrategy {
    List<TodoInstance> generateInstances(Todo todo);

    Frequency getFrequency();

    default boolean shouldGenerateMoreInstances(LocalDateTime currentStart, RepeatInfo repeatInfo, int size) {
        return (repeatInfo.getUntil() == null || !currentStart.toLocalDate().isAfter(repeatInfo.getUntil()))
                && (repeatInfo.getCount() == null || size < repeatInfo.getCount());
    }
}
