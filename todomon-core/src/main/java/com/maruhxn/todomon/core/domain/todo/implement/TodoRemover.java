package com.maruhxn.todomon.core.domain.todo.implement;

import com.maruhxn.todomon.core.domain.todo.dao.TodoRepository;
import com.maruhxn.todomon.core.domain.todo.domain.Todo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TodoRemover {

    private final TodoRepository todoRepository;

    public void remove(Todo todo) {
        todoRepository.delete(todo);
    }
}
