package com.maruhxn.todomon.core.domain.todo.implement;

import com.maruhxn.todomon.core.domain.todo.dao.TodoInstanceRepository;
import com.maruhxn.todomon.core.domain.todo.domain.TodoInstance;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TodoInstanceReader {

    private final TodoInstanceRepository todoInstanceRepository;

    public TodoInstance findById(Long id) {
        return todoInstanceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_TODO));
    }

    public TodoInstance findByIdWithTodo(Long id) {
        return todoInstanceRepository.findTodoInstanceWithTodo(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_TODO));
    }

    public List<TodoInstance> findAllByTodoId(Long todoId) {
        return todoInstanceRepository.findAllByTodo_Id(todoId);
    }

    public List<TodoInstance> findAllByWriterIdAndDate(Long memberId, LocalDateTime startAt, LocalDateTime endAt) {
        return todoInstanceRepository.findAllByWriterIdAndDate(memberId, startAt, endAt);
    }
}
