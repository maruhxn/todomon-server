package com.maruhxn.todomon.domain.todo.application;

import com.maruhxn.todomon.domain.member.domain.Member;
import com.maruhxn.todomon.domain.todo.dao.TodoInstanceRepository;
import com.maruhxn.todomon.domain.todo.dao.TodoRepository;
import com.maruhxn.todomon.domain.todo.dto.response.TodoItem;
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

    public List<TodoItem> getTodosByDay(LocalDate date, Member member) {
        LocalDateTime startAt = date.atStartOfDay();
        LocalDateTime endAt = LocalDateTime.of(date, LocalTime.of(23, 59, 59, 999999999));

        return getTodoItems(member, startAt, endAt);
    }

    public List<TodoItem> getTodosByWeek(LocalDate startOfWeek, Member member) {
        LocalDateTime startAt = startOfWeek.atStartOfDay();
        LocalDateTime endAt = LocalDateTime.of(
                startOfWeek.plusDays(6),
                LocalTime.of(23, 59, 59, 999999999)
        );

        return getTodoItems(member, startAt, endAt);
    }

    public List<TodoItem> getTodosByMonth(YearMonth yearMonth, Member member) {
        LocalDateTime startAt = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endAt = LocalDateTime.of(
                yearMonth.atEndOfMonth(),
                LocalTime.of(23, 59, 59, 999999999)
        );

        return getTodoItems(member, startAt, endAt);
    }


    private List<TodoItem> getTodoItems(Member member, LocalDateTime startAt, LocalDateTime endAt) {
        return Stream.concat(
                todoRepository
                        .findSingleTodosByWriterIdAndDate(member.getId(), startAt, endAt)
                        .stream()
                        .map(TodoItem::from),
                todoInstanceRepository
                        .findAllByWriterIdAndDate(member.getId(), startAt, endAt)
                        .stream()
                        .map(TodoItem::from)
        ).toList();
    }

}
