package com.maruhxn.todomon.core.domain.todo.application;

import com.maruhxn.todomon.core.domain.todo.dto.response.TodoItem;
import com.maruhxn.todomon.core.domain.todo.implement.TodoInstanceReader;
import com.maruhxn.todomon.core.domain.todo.implement.TodoReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TodoQueryService {

    private final TodoReader todoReader;
    private final TodoInstanceReader todoInstanceReader;

    public List<TodoItem> getTodosByDay(LocalDate date, Long memberId) {
        log.debug("일간 투두 조회 === 유저 아이디: {}, 날짜: {}", memberId, date);
        LocalDateTime startAt = date.atStartOfDay();
        LocalDateTime endAt = LocalDateTime.of(date, LocalTime.of(23, 59, 59, 999999));

        return this.getTodoItems(memberId, startAt, endAt);
    }

    public List<TodoItem> getTodosByWeek(LocalDate startOfWeek, Long memberId) {
        log.debug("주간 투두 조회 === 유저 아이디: {}, 주: {}", memberId, startOfWeek);
        LocalDateTime startAt = startOfWeek.atStartOfDay();
        LocalDateTime endAt = LocalDateTime.of(
                startOfWeek.plusDays(6),
                LocalTime.of(23, 59, 59, 999999)
        );

        return this.getTodoItems(memberId, startAt, endAt);
    }

    public List<TodoItem> getTodosByMonth(YearMonth yearMonth, Long memberId) {
        log.debug("월간 투두 조회 === 유저 아이디: {}, 월: {}", memberId, yearMonth);
        LocalDateTime startAt = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endAt = LocalDateTime.of(
                yearMonth.atEndOfMonth(),
                LocalTime.of(23, 59, 59, 999999)
        );

        return this.getTodoItems(memberId, startAt, endAt);
    }


    private List<TodoItem> getTodoItems(Long memberId, LocalDateTime startAt, LocalDateTime endAt) {
        return Stream.concat(
                todoReader
                        .findAllByWriterIdAndDate(memberId, startAt, endAt)
                        .stream()
                        .map(TodoItem::from),
                todoInstanceReader
                        .findAllByWriterIdAndDate(memberId, startAt, endAt)
                        .stream()
                        .map(TodoItem::from)
        ).toList();
    }

}
