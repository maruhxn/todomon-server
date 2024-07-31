package com.maruhxn.todomon.util;

import com.maruhxn.todomon.domain.member.domain.Member;
import com.maruhxn.todomon.domain.todo.dao.RepeatInfoRepository;
import com.maruhxn.todomon.domain.todo.dao.TodoInstanceRepository;
import com.maruhxn.todomon.domain.todo.dao.TodoRepository;
import com.maruhxn.todomon.domain.todo.domain.RepeatInfo;
import com.maruhxn.todomon.domain.todo.domain.Todo;
import com.maruhxn.todomon.domain.todo.domain.TodoInstance;
import com.maruhxn.todomon.domain.todo.dto.request.UpdateTodoReq;
import com.maruhxn.todomon.global.util.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
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
        repeatInfoRepository.save(repeatInfo);
        todoRepository.save(todo);

        createTodoInstances(todo);
        return todo;
    }

    private void createTodoInstances(Todo todo) {
        RepeatInfo repeatInfo = todo.getRepeatInfo();
        LocalDateTime startAt = todo.getStartAt();
        LocalDateTime endAt = todo.getEndAt();

        List<TodoInstance> instances = new ArrayList<>();

        switch (repeatInfo.getFrequency()) {
            case DAILY -> instances.addAll(generateDailyInstances(todo, startAt, endAt, repeatInfo));
            case WEEKLY -> instances.addAll(generateWeeklyInstances(todo, startAt, endAt, repeatInfo));
            case MONTHLY -> instances.addAll(generateMonthlyInstances(todo, startAt, endAt, repeatInfo));
        }

        if (instances.size() > 1) { // 최소 반복 횟수를 넘지 못하면 인스턴스를 생성하지 않고, 단일 투두로 처리
            todo.setTodoInstances(instances);
            todoInstanceRepository.saveAll(instances);
            LocalDateTime repeatStartAt = instances.get(0).getStartAt();
            LocalDateTime repeatEndAt = instances.get(instances.size() - 1).getEndAt();
            todo.update(UpdateTodoReq.builder()
                    .startAt(repeatStartAt)
                    .endAt(repeatEndAt)
                    .build());
        }

    }

    private List<TodoInstance> generateMonthlyInstances(Todo todo, LocalDateTime startAt, LocalDateTime endAt, RepeatInfo repeatInfo) {
        List<TodoInstance> instances = new ArrayList<>();
        Integer dayOfMonth = repeatInfo.getByMonthDay();
        LocalDateTime currentStart = startAt;
        LocalDateTime currentEnd = endAt;

        currentStart = adjustDayOfMonth(currentStart, dayOfMonth, repeatInfo.getInterval());
        currentEnd = adjustDayOfMonth(currentEnd, dayOfMonth, repeatInfo.getInterval());

        if (startAt.isAfter(currentStart)) {
            currentStart = currentStart.plusMonths(repeatInfo.getInterval());
            currentEnd = currentEnd.plusMonths(repeatInfo.getInterval());
        }

        while (shouldGenerateMoreInstances(currentStart, repeatInfo, instances.size())) {
            currentStart = adjustDayOfMonth(currentStart, dayOfMonth, repeatInfo.getInterval());
            currentEnd = adjustDayOfMonth(currentEnd, dayOfMonth, repeatInfo.getInterval());
            instances.add(TodoInstance.of(todo, currentStart, currentEnd));
            currentStart = currentStart.plusMonths(repeatInfo.getInterval());
            currentEnd = currentEnd.plusMonths(repeatInfo.getInterval());
        }

        return instances;
    }

    private LocalDateTime adjustDayOfMonth(LocalDateTime dateTime, int dayOfMonth, int interval) {
        int maxDayOfMonth = dateTime.getMonth().length(dateTime.toLocalDate().isLeapYear());
        if (dayOfMonth > maxDayOfMonth) {
            return dateTime.plusMonths(interval).withDayOfMonth(dayOfMonth);
        }
        return dateTime.withDayOfMonth(dayOfMonth);
    }

    private List<TodoInstance> generateWeeklyInstances(Todo todo, LocalDateTime startAt, LocalDateTime endAt, RepeatInfo repeatInfo) {
        List<TodoInstance> instances = new ArrayList<>();
        LocalDateTime currentStart = startAt;
        LocalDateTime currentEnd = endAt;

        // 주어진 요일 목록 파싱
        List<DayOfWeek> byDays = Arrays.stream(repeatInfo.getByDay().split(","))
                .map(TimeUtil::convertToDayOfWeek)
                .toList();

        while (shouldGenerateMoreInstances(currentStart, repeatInfo, instances.size())) {
            if (byDays.contains(currentStart.getDayOfWeek())) { // 현재 날짜가 byDays에 포함되는지 확인
                instances.add(TodoInstance.of(todo, currentStart, currentEnd)); // 포함된다면 인스턴스 생성
            }
            currentStart = currentStart.plusDays(1);
            currentEnd = currentEnd.plusDays(1);
            if (currentStart.getDayOfWeek() == DayOfWeek.MONDAY) {
                currentStart = currentStart.plusWeeks(repeatInfo.getInterval() - 1);
                currentEnd = currentEnd.plusDays(repeatInfo.getInterval() - 1);
            }

        }

        return instances;
    }

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


    // 현재 시간이 규칙에 의해 정의된 종료 시점이나 반복 횟수 조건을 초과하지 않았는지 확인
    private boolean shouldGenerateMoreInstances(LocalDateTime currentStart, RepeatInfo repeatInfo, int size) {
        if (repeatInfo.getUntil() != null && currentStart.isAfter(repeatInfo.getUntil().plusDays(1).atStartOfDay()))
            return false;
        if (repeatInfo.getCount() != null && size >= repeatInfo.getCount()) return false;
        return true;
    }

}
