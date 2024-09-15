package com.maruhxn.todomon.core.domain.todo.application;

import com.maruhxn.todomon.core.domain.todo.dao.TodoInstanceRepository;
import com.maruhxn.todomon.core.domain.todo.dao.TodoRepository;
import com.maruhxn.todomon.core.domain.todo.dto.response.TodoItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TodoQueryService {

    private final TodoRepository todoRepository;
    private final TodoInstanceRepository todoInstanceRepository;

    public List<TodoItem> getTodosByDay(LocalDate date, Long memberId) {
        LocalDateTime startAt = date.atStartOfDay();
        LocalDateTime endAt = LocalDateTime.of(date, LocalTime.of(23, 59, 59, 999999999));

        return getTodoItems(memberId, startAt, endAt);
    }

    public List<TodoItem> getTodosByWeek(LocalDate startOfWeek, Long memberId) {
        LocalDateTime startAt = startOfWeek.atStartOfDay();
        LocalDateTime endAt = LocalDateTime.of(
                startOfWeek.plusDays(6),
                LocalTime.of(23, 59, 59, 999999999)
        );

        return getTodoItems(memberId, startAt, endAt);
    }

    public List<TodoItem> getTodosByMonth(YearMonth yearMonth, Long memberId) {
        LocalDateTime startAt = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endAt = LocalDateTime.of(
                yearMonth.atEndOfMonth(),
                LocalTime.of(23, 59, 59, 999999999)
        );

        return getTodoItems(memberId, startAt, endAt);
    }


    private List<TodoItem> getTodoItems(Long memberId, LocalDateTime startAt, LocalDateTime endAt) {
        return Stream.concat(
                todoRepository
                        .findSingleTodosByWriterIdAndDate(memberId, startAt, endAt)
                        .stream()
                        .map(TodoItem::from),
                todoInstanceRepository
                        .findAllByWriterIdAndDate(memberId, startAt, endAt)
                        .stream()
                        .map(TodoItem::from)
        ).toList();
    }

}
