package com.maruhxn.todomon.core.domain.todo.implement;

import com.maruhxn.todomon.core.domain.todo.dao.TodoInstanceRepository;
import com.maruhxn.todomon.core.domain.todo.domain.Todo;
import com.maruhxn.todomon.core.domain.todo.domain.TodoInstance;
import com.maruhxn.todomon.core.domain.todo.dto.request.TargetType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TodoInstanceRemover {

    private final TodoInstanceRepository todoInstanceRepository;
    private final TodoRemover todoRemover;

    public void remove(TodoInstance todoInstance, TargetType targetType) {
        Todo todo = todoInstance.getTodo();
        List<TodoInstance> todoInstances = todo.getTodoInstances();

        switch (targetType) {
            case THIS_TASK -> {
                todoInstanceRepository.delete(todoInstance);
                todoInstances.remove(todoInstance);
            }
            case ALL_TASKS -> {
                todoInstanceRepository.deleteAllByTodo_Id(todo.getId());
                todoInstances.clear();
            }
        }

        if (todoInstances.isEmpty()) todoRemover.remove(todo);
    }

    public void removeAllByTodoId(Long todoId) {
        todoInstanceRepository.deleteAllByTodo_Id(todoId); // 삭제할 대상이 없어도 예외 X
    }
}
