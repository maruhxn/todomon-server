package com.maruhxn.todomon.core.domain.todo.implement;

import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.todo.domain.Todo;
import com.maruhxn.todomon.core.domain.todo.domain.TodoInstance;
import com.maruhxn.todomon.core.domain.todo.dto.request.TargetType;
import com.maruhxn.todomon.core.domain.todo.dto.request.UpdateTodoReq;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TodoInstanceModifier {

    private final RewardManager rewardManager;
    private final RepeatInfoWriter repeatInfoWriter;
    private final TodoModifier todoModifier;
    private final TodoInstanceCreator todoInstanceCreator;
    private final TodoInstanceReader todoInstanceReader;
    private final TodoInstanceRemover todoInstanceRemover;

    public void updateTodoInstance(TodoInstance todoInstance, TargetType targetType, UpdateTodoReq req) {
        switch (targetType) {
            case THIS_TASK -> this.updateSingleInstance(todoInstance, req);
            case ALL_TASKS -> this.updateAllInstances(todoInstance, req);
            default -> {
                log.info("잘못된 투두 인스턴스 수정 타입 전달 === 인스턴스 아이디: {}, 타입: {}", todoInstance.getId(), targetType);
                throw new BadRequestException(ErrorCode.BAD_REQUEST, "잘못된 type입니다.");
            }
        }
    }

    // 시간 정보 수정은 THIS_TASK 타입만 가능
    private void updateSingleInstance(TodoInstance todoInstance, UpdateTodoReq req) {
        // 반복 정보 수정은 ALL_TASKS 타입만 가능
        if (req.getRepeatInfoReqItem() != null) {
            log.info("단일 인스턴스에 대한 반복 수정 요청 === 인스턴스 아이디: {}, 요청 정보: {}", todoInstance.getId(), req);
            throw new BadRequestException(ErrorCode.BAD_REQUEST, "전체 인스턴스에 대해서만 반복 정보 수정이 가능합니다.");
        }

        this.validateInstanceTime(todoInstance, req);

        todoInstance.update(req);
    }

    private void validateInstanceTime(TodoInstance todoInstance, UpdateTodoReq req) {
        if ((req.getEndAt() == null && todoInstance.getEndAt().isBefore(req.getStartAt()))
                || (req.getStartAt() == null && todoInstance.getStartAt().isAfter(req.getEndAt()))
        ) {
            log.info("단일 인스턴스 수정 시잘못된 시간 정보 전달 === 인스턴스 아이디: {}, 요청 정보: {}", todoInstance.getId(), req);
            throw new BadRequestException(ErrorCode.BAD_REQUEST, "시작 시각은 종료 시각보다 이전이어야 합니다.");
        }
    }

    private void updateAllInstances(TodoInstance todoInstance, UpdateTodoReq req) {
        this.validateTimeInfoExist(req);

        Todo todo = todoInstance.getTodo();
        todoModifier.updateTodo(todo, req); // todo 먼저 업데이트
        todoInstance.update(req);

        if (req.getRepeatInfoReqItem() != null) {
            this.updateAllWithNewRepeatInfo(todo, req);
            todoInstanceCreator.generateAndSaveInstances(todo);
        } else {
            this.updateAllWithoutNewRepeatInfo(todoInstance, req);
        }
    }

    private void validateTimeInfoExist(UpdateTodoReq req) {
        if (req.getStartAt() != null || req.getEndAt() != null) {
            log.debug("복수 인스턴스에 대한 시간 정보 수정 요청 === 요청 정보: {}", req);
            throw new BadRequestException(ErrorCode.BAD_REQUEST, "시간 정보 수정은 단일 인스턴스에 대해서만 수정 가능합니다.");
        }
    }

    private void updateAllWithNewRepeatInfo(Todo todo, UpdateTodoReq req) {
        repeatInfoWriter.removeOldRepeatInfo(todo);
        todoInstanceRemover.removeAllByTodoId(todo.getId());
        todo.updateEndAtTemporally();
        repeatInfoWriter.updateRepeatInfo(req, todo);
    }

    private void updateAllWithoutNewRepeatInfo(TodoInstance todoInstance, UpdateTodoReq req) {
        todoInstanceReader.findAllByTodoId(todoInstance.getTodo().getId())
                .forEach(instance -> instance.update(req));
    }

    public void doStatusUpdateProcess(TodoInstance todoInstance, boolean isDone, Member member) {
        if (isDone) {
            if (!todoInstance.isDone()) {
                todoInstance.updateIsDone(true);
                rewardManager.rewardForInstance(todoInstance, member);
            }
        } else {
            if (todoInstance.isDone()) {
                rewardManager.withdrawRewardForInstance(todoInstance, member);
                todoInstance.updateIsDone(false);
            }
        }
    }
}
