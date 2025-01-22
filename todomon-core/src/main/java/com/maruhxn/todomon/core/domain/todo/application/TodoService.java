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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
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
        log.info("투두 생성 === 유저 아이디: {}, 요청 정보: {}", memberId, req);
        Member member = memberReader.findById(memberId);
        Todo todo = todoCreator.createTodo(member, req);
        if (todo.getRepeatInfo() != null) {
            log.info("투두 인스턴스 생성 === 유저 아이디: {}, 투두 아이디: {}, 요청 정보: {}", memberId, todo.getId(), req);
            todoInstanceCreator.generateAndSaveInstances(todo);
        }
    }

    @IsTodayTodoOrAdmin
    @IsMyTodoOrAdmin
    public void update(Long objectId, UpdateAndDeleteTodoQueryParams params, UpdateTodoReq req) {
        if (params.getIsInstance()) {
            log.info("투두 인스턴스 수정 === 투두 인스턴스 아이디: {}, 요청 타입: {}, 요청 정보: {}", objectId, params.getTargetType(), req);
            TodoInstance todoInstance = todoInstanceReader.findByIdWithTodo(objectId);
            todoInstanceModifier.updateTodoInstance(todoInstance, params.getTargetType(), req);
        } else {
            log.info("투두 수정 === 투두 아이디: {}, 요청 정보: {}", objectId, req);
            Todo todo = todoReader.findById(objectId);
            todoModifier.updateTodo(todo, req);
        }
    }

    @IsTodayTodoOrAdmin
    @IsMyTodoOrAdmin
    public void updateStatusAndReward(Long objectId, boolean isInstance, Long memberId, UpdateTodoStatusReq req) {
        Member member = memberReader.findMemberWithDiligenceUsingLock(memberId);
        if (isInstance) {
            log.info("투두 인스턴스 상태 업데이트 === 유저 아이디: {}, 투두 인스턴스 아이디: {}, 요청 정보: {}", memberId, objectId, req);
            TodoInstance todoInstance = todoInstanceReader.findByIdWithTodo(objectId);
            todoInstanceModifier.doStatusUpdateProcess(todoInstance, req.getIsDone(), member);
        } else {
            log.info("투두 상태 업데이트 === 유저 아이디: {}, 투두 아이디: {}, 요청 정보: {}", memberId, objectId, req);
            Todo todo = todoReader.findById(objectId);
            todoModifier.doStatusUpdateProcess(todo, req.getIsDone(), member);
        }
    }

    @IsMyTodoOrAdmin
    public void deleteTodo(Long objectId, UpdateAndDeleteTodoQueryParams params) {
        if (params.getIsInstance()) {
            log.info("투두 인스턴스 삭제 === 투두 인스턴스 아이디: {}, 요청 타입: {}", objectId, params.getTargetType());
            TodoInstance todoInstance = todoInstanceReader.findById(objectId);
            todoInstanceRemover.remove(todoInstance, params.getTargetType());
        } else {
            log.info("투두 삭제 === 투두 아이디: {}", objectId);
            Todo todo = todoReader.findById(objectId);
            todoRemover.remove(todo);
        }
    }

}
