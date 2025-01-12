package com.maruhxn.todomon.util;

import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.todo.application.TodoInstanceService;
import com.maruhxn.todomon.core.domain.todo.dao.RepeatInfoRepository;
import com.maruhxn.todomon.core.domain.todo.dao.TodoRepository;
import com.maruhxn.todomon.core.domain.todo.domain.RepeatInfo;
import com.maruhxn.todomon.core.domain.todo.domain.Todo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;

import java.time.LocalDateTime;

@Profile("test")
public class TestTodoFactory {

    @Autowired
    TodoRepository todoRepository;

    @Autowired
    RepeatInfoRepository repeatInfoRepository;

    @Autowired
    TodoInstanceService todoInstanceService;

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

        todoInstanceService.generateAndSaveInstances(todo);
        return todo;
    }

}
