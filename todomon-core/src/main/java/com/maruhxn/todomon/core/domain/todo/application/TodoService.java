package com.maruhxn.todomon.core.domain.todo.application;

import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.member.implement.MemberReader;
import com.maruhxn.todomon.core.domain.todo.domain.Todo;
import com.maruhxn.todomon.core.domain.todo.domain.TodoInstance;
import com.maruhxn.todomon.core.domain.todo.dto.request.CreateTodoReq;
import com.maruhxn.todomon.core.domain.todo.dto.request.UpdateAndDeleteTodoQueryParams;
import com.maruhxn.todomon.core.domain.todo.dto.request.UpdateTodoReq;
import com.maruhxn.todomon.core.domain.todo.dto.request.UpdateTodoStatusReq;
import com.maruhxn.todomon.core.domain.todo.implement.*;
import com.maruhxn.todomon.core.global.auth.checker.IsMyTodoOrAdmin;
import com.maruhxn.todomon.core.global.util.validation.IsTodayTodoOrAdmin;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * create, update 시 입력받는 startAt, endAt에 대한 유효성 검증 필요
 */
@Service
@Transactional
@RequiredArgsConstructor
public class TodoService {

    private final MemberReader memberReader;
    private final TodoReader todoReader;
    private final TodoCreator todoCreator;
    private final TodoModifier todoModifier;
    private final TodoRemover todoRemover;
    private final TodoInstanceCreator todoInstanceCreator;
    private final TodoInstanceModifier todoInstanceModifier;
    private final TodoInstanceRemover todoInstanceRemover;
    private final TodoInstanceReader todoInstanceReader;

    public void create(Long memberId, CreateTodoReq req) {
        Member member = memberReader.findById(memberId);
        Todo todo = todoCreator.createTodo(member, req);
        if (todo.getRepeatInfo() != null) {
            todoInstanceCreator.generateAndSaveInstances(todo);
        }
    }

    @IsTodayTodoOrAdmin
    @IsMyTodoOrAdmin
    public void update(Long objectId, UpdateAndDeleteTodoQueryParams params, UpdateTodoReq req) {
        if (params.getIsInstance()) {
            TodoInstance todoInstance = todoInstanceReader.findByIdWithTodo(objectId);
            todoInstanceModifier.updateTodoInstance(todoInstance, params.getTargetType(), req);
        } else {
            Todo todo = todoReader.findById(objectId);
            todoModifier.updateTodo(todo, req);
        }
    }

    @IsTodayTodoOrAdmin
    @IsMyTodoOrAdmin
    public void updateStatusAndReward(Long objectId, boolean isInstance, Long memberId, UpdateTodoStatusReq req) {
        Member member = memberReader.findMemberWithDiligenceUsingLock(memberId);
        if (isInstance) {
            TodoInstance todoInstance = todoInstanceReader.findByIdWithTodo(objectId);
            todoInstanceModifier.doStatusUpdateProcess(todoInstance, req.getIsDone(), member);
        } else {
            Todo todo = todoReader.findById(objectId);
            todoModifier.doStatusUpdateProcess(todo, req.getIsDone(), member);
        }
    }

    @IsMyTodoOrAdmin
    public void deleteTodo(Long objectId, UpdateAndDeleteTodoQueryParams params) {
        if (params.getIsInstance()) {
            TodoInstance todoInstance = todoInstanceReader.findById(objectId);
            todoInstanceRemover.remove(todoInstance, params.getTargetType());
        } else {
            Todo todo = todoReader.findById(objectId);
            todoRemover.remove(todo);
        }
    }

}
