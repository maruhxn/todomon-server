package com.maruhxn.todomon.core.domain.todo.implement.strategy;

import com.maruhxn.todomon.core.domain.todo.domain.Frequency;
import com.maruhxn.todomon.core.domain.todo.domain.RepeatInfo;
import com.maruhxn.todomon.core.domain.todo.domain.Todo;
import com.maruhxn.todomon.core.domain.todo.domain.TodoInstance;
import com.maruhxn.todomon.core.global.util.TimeUtil;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class WeeklyRepeatInfoStrategy implements RepeatInfoStrategy {
    @Override
    public List<TodoInstance> generateInstances(Todo todo) {
        List<TodoInstance> instances = new ArrayList<>();
        RepeatInfo repeatInfo = todo.getRepeatInfo();
        LocalDateTime currentStart = todo.getStartAt();
        LocalDateTime currentEnd = todo.getEndAt();

        List<DayOfWeek> byDays = this.convertToDayOfWeeks(repeatInfo.getByDay());

        while (this.shouldGenerateMoreInstances(currentStart, repeatInfo, instances.size())) {
            if (byDays.contains(currentStart.getDayOfWeek())) {
                instances.add(TodoInstance.of(todo, currentStart, currentEnd));
            }

            currentStart = currentStart.plusDays(1);
            currentEnd = currentEnd.plusDays(1);

            if (currentStart.getDayOfWeek() == DayOfWeek.MONDAY) {
                currentStart = currentStart.plusWeeks(repeatInfo.getInterval() - 1);
                currentEnd = currentEnd.plusWeeks(repeatInfo.getInterval() - 1);
            }
        }

        return instances;
    }

    @Override
    public Frequency getFrequency() {
        return Frequency.WEEKLY;
    }

    // 문자열을 DayOfWeek 리스트로 변환하는 로직을 분리
    private List<DayOfWeek> convertToDayOfWeeks(String byDay) {
        return Arrays.stream(byDay.split(",")).map(TimeUtil::convertToDayOfWeek).toList();
    }

}
