package com.maruhxn.todomon.util;

import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.todo.dao.RepeatInfoRepository;
import com.maruhxn.todomon.core.domain.todo.dao.TodoInstanceRepository;
import com.maruhxn.todomon.core.domain.todo.dao.TodoRepository;
import com.maruhxn.todomon.core.domain.todo.domain.RepeatInfo;
import com.maruhxn.todomon.core.domain.todo.domain.Todo;
import com.maruhxn.todomon.core.domain.todo.domain.TodoInstance;
import com.maruhxn.todomon.core.domain.todo.dto.request.UpdateTodoReq;
import com.maruhxn.todomon.core.global.util.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Profile("test")
public class TestTodoFactory {

    @Autowired
    TodoRepository todoRepository;

    @Autowired
    RepeatInfoRepository repeatInfoRepository;

    @Autowired
    TodoInstanceRepository todoInstanceRepository;

    public Todo createSingleTodo(LocalDateTime startAt, LocalDateTime endAt, boolean isAllDay, Member member) {
        Todo todo = Todo.builder()
                .content("테스트")
                .startAt(startAt)
                .endAt(endAt)
                .isAllDay(isAllDay)
                .writer(member)
                .build();

        return todoRepository.save(todo);
    }

    public Todo createRepeatedTodo(LocalDateTime startAt, LocalDateTime endAt, boolean isAllDay, Member member, RepeatInfo repeatInfo) {
        Todo todo = Todo.builder()
                .content("테스트")
                .startAt(startAt)
                .endAt(endAt)
                .isAllDay(isAllDay)
                .writer(member)
                .build();
        todo.setRepeatInfo(repeatInfo);
        todoRepository.save(todo);
        repeatInfoRepository.save(repeatInfo);

        createTodoInstances(todo);
        return todo;
    }

    private void createTodoInstances(Todo todo) {
        List<TodoInstance> instances = generateInstances(todo);

        if (instances.size() > 1) {
            todo.setTodoInstances(instances);
            todoInstanceRepository.saveAll(instances);
            updateTodoDateRange(todo, instances);
        }
    }

    // 공통 로직을 함수로 분리하여 코드 중복 제거
    private void updateTodoDateRange(Todo todo, List<TodoInstance> instances) {
        LocalDateTime repeatStartAt = instances.get(0).getStartAt();
        LocalDateTime repeatEndAt = instances.get(instances.size() - 1).getEndAt();
        todo.update(UpdateTodoReq.builder()
                .startAt(repeatStartAt)
                .endAt(repeatEndAt)
                .build());
    }

    private List<TodoInstance> generateInstances(Todo todo) {
        RepeatInfo repeatInfo = todo.getRepeatInfo();
        LocalDateTime startAt = todo.getStartAt();
        LocalDateTime endAt = todo.getEndAt();

        switch (repeatInfo.getFrequency()) {
            case DAILY:
                return generateDailyInstances(todo, startAt, endAt, repeatInfo);
            case WEEKLY:
                return generateWeeklyInstances(todo, startAt, endAt, repeatInfo);
            case MONTHLY:
                return generateMonthlyInstances(todo, startAt, endAt, repeatInfo);
            default:
                return Collections.emptyList();
        }
    }

    private List<TodoInstance> generateMonthlyInstances(Todo todo, LocalDateTime startAt, LocalDateTime endAt, RepeatInfo repeatInfo) {
        List<TodoInstance> instances = new ArrayList<>();
        LocalDateTime currentStart = adjustDayOfMonth(startAt, repeatInfo.getByMonthDay(), repeatInfo.getInterval());
        LocalDateTime currentEnd = adjustDayOfMonth(endAt, repeatInfo.getByMonthDay(), repeatInfo.getInterval());

        while (shouldGenerateMoreInstances(currentStart, repeatInfo, instances.size())) {
            currentStart = adjustDayOfMonth(currentStart, repeatInfo.getByMonthDay(), repeatInfo.getInterval());
            currentEnd = adjustDayOfMonth(currentEnd, repeatInfo.getByMonthDay(), repeatInfo.getInterval());
            instances.add(TodoInstance.of(todo, currentStart, currentEnd));
            currentStart = currentStart.plusMonths(repeatInfo.getInterval());
            currentEnd = currentEnd.plusMonths(repeatInfo.getInterval());
        }

        return instances;
    }

    // 불필요한 조건문 제거 및 메소드 간소화
    private LocalDateTime adjustDayOfMonth(LocalDateTime dateTime, int dayOfMonth, int interval) {
        int maxDayOfMonth = dateTime.getMonth().length(dateTime.toLocalDate().isLeapYear());
        if (dayOfMonth > maxDayOfMonth) {
            return dateTime.plusMonths(interval).withDayOfMonth(dayOfMonth);
        }
        return dateTime.withDayOfMonth(dayOfMonth);
    }

    // 주 반복 인스턴스 생성 로직 개선
    private List<TodoInstance> generateWeeklyInstances(Todo todo, LocalDateTime startAt, LocalDateTime endAt, RepeatInfo repeatInfo) {
        List<TodoInstance> instances = new ArrayList<>();
        LocalDateTime currentStart = startAt;
        LocalDateTime currentEnd = endAt;
        List<DayOfWeek> byDays = convertToDayOfWeeks(repeatInfo.getByDay());

        while (shouldGenerateMoreInstances(currentStart, repeatInfo, instances.size())) {
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

    // 문자열을 DayOfWeek 리스트로 변환하는 로직을 분리
    private List<DayOfWeek> convertToDayOfWeeks(String byDay) {
        return Arrays.stream(byDay.split(",")).map(TimeUtil::convertToDayOfWeek).toList();
    }


    // 일 반복 인스턴스 생성 로직 개선 및 리팩터링
    private List<TodoInstance> generateDailyInstances(Todo todo, LocalDateTime startAt, LocalDateTime endAt, RepeatInfo repeatInfo) {
        List<TodoInstance> instances = new ArrayList<>();
        LocalDateTime currentStart = startAt;
        LocalDateTime currentEnd = endAt;

        while (shouldGenerateMoreInstances(currentStart, repeatInfo, instances.size())) {
            instances.add(TodoInstance.of(todo, currentStart, currentEnd));
            currentStart = currentStart.plusDays(repeatInfo.getInterval());
            currentEnd = currentEnd.plusDays(repeatInfo.getInterval());
        }

        return instances;
    }


    // 조건문 간소화
    private boolean shouldGenerateMoreInstances(LocalDateTime currentStart, RepeatInfo repeatInfo, int size) {
        return (repeatInfo.getUntil() == null || !currentStart.toLocalDate().isAfter(repeatInfo.getUntil()))
                && (repeatInfo.getCount() == null || size < repeatInfo.getCount());
    }


}
