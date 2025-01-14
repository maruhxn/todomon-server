package com.maruhxn.todomon.core.domain.todo.implement;

import com.maruhxn.todomon.core.domain.todo.implement.strategy.RepeatInfoStrategy;
import com.maruhxn.todomon.core.domain.todo.dao.TodoInstanceRepository;
import com.maruhxn.todomon.core.domain.todo.domain.RepeatInfo;
import com.maruhxn.todomon.core.domain.todo.domain.Todo;
import com.maruhxn.todomon.core.domain.todo.domain.TodoInstance;
import com.maruhxn.todomon.core.domain.todo.dto.request.UpdateTodoReq;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TodoInstanceCreator {

    private final TodoInstanceRepository todoInstanceRepository;
    private final RepeatInfoStrategyFactory strategyFactory;

    public void generateAndSaveInstances(Todo todo) {
        RepeatInfo repeatInfo = todo.getRepeatInfo();
        RepeatInfoStrategy strategy = strategyFactory.getStrategy(repeatInfo.getFrequency());
        List<TodoInstance> instances = strategy.generateInstances(todo);

        if (!instances.isEmpty()) {
            todo.setTodoInstances(instances);
            todoInstanceRepository.saveAll(instances);
            this.updateTodoDateRange(todo, instances);
        }
    }

    private void updateTodoDateRange(Todo todo, List<TodoInstance> instances) {
        LocalDateTime repeatStartAt = instances.get(0).getStartAt();
        LocalDateTime repeatEndAt = instances.get(instances.size() - 1).getEndAt();
        todo.update(UpdateTodoReq.builder()
                .startAt(repeatStartAt)
                .endAt(repeatEndAt)
                .build());
    }
}
