package com.maruhxn.todomon.core.domain.todo.implement;

import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.todo.dao.TodoRepository;
import com.maruhxn.todomon.core.domain.todo.domain.RepeatInfo;
import com.maruhxn.todomon.core.domain.todo.domain.Todo;
import com.maruhxn.todomon.core.domain.todo.dto.request.CreateTodoReq;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TodoCreator {
    private final TodoRepository todoRepository;
    private final RepeatInfoWriter repeatInfoWriter;

    public Todo createTodo(Member member, CreateTodoReq req) {
        Todo todo = req.toEntity(member);
        if (req.getRepeatInfoReqItem() != null) {
            RepeatInfo repeatInfo = repeatInfoWriter.createRepeatInfo(req.getRepeatInfoReqItem());
            todo.setRepeatInfo(repeatInfo); // Cascade.PERSIST에 의해 함께 저장됨
        }

        return todoRepository.save(todo);
    }
}
