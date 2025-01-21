package com.maruhxn.todomon.core.util;

import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.todo.implement.RepeatInfoStrategyFactory;
import com.maruhxn.todomon.core.domain.todo.implement.strategy.RepeatInfoStrategy;
import com.maruhxn.todomon.core.domain.todo.dao.RepeatInfoRepository;
import com.maruhxn.todomon.core.domain.todo.dao.TodoInstanceRepository;
import com.maruhxn.todomon.core.domain.todo.dao.TodoRepository;
import com.maruhxn.todomon.core.domain.todo.domain.RepeatInfo;
import com.maruhxn.todomon.core.domain.todo.domain.Todo;
import com.maruhxn.todomon.core.domain.todo.domain.TodoInstance;
import com.maruhxn.todomon.core.domain.todo.dto.request.UpdateTodoReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;

import java.time.LocalDateTime;
import java.util.List;

@Profile("test")
public class TestTodoFactory {

    @Autowired
    TodoRepository todoRepository;

    @Autowired
    RepeatInfoRepository repeatInfoRepository;

    @Autowired
    TodoInstanceRepository todoInstanceRepository;

    @Autowired
    RepeatInfoStrategyFactory strategyFactory;

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

        this.generateAndSaveInstances(todo);
        return todo;
    }

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
