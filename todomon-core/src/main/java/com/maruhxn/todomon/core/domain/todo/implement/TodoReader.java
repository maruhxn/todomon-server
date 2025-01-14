package com.maruhxn.todomon.core.domain.todo.implement;

import com.maruhxn.todomon.core.domain.todo.dao.TodoRepository;
import com.maruhxn.todomon.core.domain.todo.domain.Todo;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TodoReader {

    private final TodoRepository todoRepository;

    public Todo findById(Long id) {
        return todoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_TODO));
    }

    public List<Todo> findAllByWriterIdAndDate(Long memberId, LocalDateTime startAt, LocalDateTime endAt) {
        return todoRepository
                .findSingleTodosByWriterIdAndDate(memberId, startAt, endAt);
    }

}
