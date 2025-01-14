package com.maruhxn.todomon.core.domain.todo.implement.strategy;

import com.maruhxn.todomon.core.domain.todo.domain.Frequency;
import com.maruhxn.todomon.core.domain.todo.domain.RepeatInfo;
import com.maruhxn.todomon.core.domain.todo.domain.Todo;
import com.maruhxn.todomon.core.domain.todo.domain.TodoInstance;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class MonthlyRepeatInfoStrategy implements RepeatInfoStrategy {

    @Override
    public List<TodoInstance> generateInstances(Todo todo) {
        List<TodoInstance> instances = new ArrayList<>();
        RepeatInfo repeatInfo = todo.getRepeatInfo();
        LocalDateTime currentStart = this.adjustDayOfMonth(todo.getStartAt(), repeatInfo.getByMonthDay(), repeatInfo.getInterval());
        LocalDateTime currentEnd = this.adjustDayOfMonth(todo.getEndAt(), repeatInfo.getByMonthDay(), repeatInfo.getInterval());

        while (this.shouldGenerateMoreInstances(currentStart, repeatInfo, instances.size())) {
            currentStart = this.adjustDayOfMonth(currentStart, repeatInfo.getByMonthDay(), repeatInfo.getInterval());
            currentEnd = this.adjustDayOfMonth(currentEnd, repeatInfo.getByMonthDay(), repeatInfo.getInterval());
            instances.add(TodoInstance.of(todo, currentStart, currentEnd));
            currentStart = currentStart.plusMonths(repeatInfo.getInterval());
            currentEnd = currentEnd.plusMonths(repeatInfo.getInterval());
        }

        return instances;
    }

    @Override
    public Frequency getFrequency() {
        return Frequency.MONTHLY;
    }

    // 불필요한 조건문 제거 및 메소드 간소화
    private LocalDateTime adjustDayOfMonth(LocalDateTime dateTime, int dayOfMonth, int interval) {
        int maxDayOfMonth = dateTime.getMonth().length(dateTime.toLocalDate().isLeapYear());
        if (dayOfMonth > maxDayOfMonth) {
            return dateTime.plusMonths(interval).withDayOfMonth(dayOfMonth);
        }
        return dateTime.withDayOfMonth(dayOfMonth);
    }
}
