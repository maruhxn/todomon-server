package com.maruhxn.todomon.core.domain.todo.application.strategy;

import com.maruhxn.todomon.core.domain.todo.domain.Frequency;
import com.maruhxn.todomon.core.domain.todo.domain.RepeatInfo;
import com.maruhxn.todomon.core.domain.todo.domain.Todo;
import com.maruhxn.todomon.core.domain.todo.domain.TodoInstance;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class DailyRepeatInfoStrategy implements RepeatInfoStrategy {

    @Override
    public List<TodoInstance> generateInstances(Todo todo) {
        List<TodoInstance> instances = new ArrayList<>();
        LocalDateTime currentStart = todo.getStartAt();
        LocalDateTime currentEnd = todo.getEndAt();
        RepeatInfo repeatInfo = todo.getRepeatInfo();

        while (this.shouldGenerateMoreInstances(currentStart, repeatInfo, instances.size())) {
            instances.add(TodoInstance.of(todo, currentStart, currentEnd));
            currentStart = currentStart.plusDays(repeatInfo.getInterval());
            currentEnd = currentEnd.plusDays(repeatInfo.getInterval());
        }

        return instances;
    }

    @Override
    public Frequency getFrequency() {
        return Frequency.DAILY;
    }

}
