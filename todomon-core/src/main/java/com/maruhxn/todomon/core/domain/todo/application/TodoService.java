package com.maruhxn.todomon.core.domain.todo.application;

import com.maruhxn.todomon.core.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.todo.dao.TodoRepository;
import com.maruhxn.todomon.core.domain.todo.domain.RepeatInfo;
import com.maruhxn.todomon.core.domain.todo.domain.Todo;
import com.maruhxn.todomon.core.domain.todo.dto.request.CreateTodoReq;
import com.maruhxn.todomon.core.domain.todo.dto.request.UpdateAndDeleteTodoQueryParams;
import com.maruhxn.todomon.core.domain.todo.dto.request.UpdateTodoReq;
import com.maruhxn.todomon.core.domain.todo.dto.request.UpdateTodoStatusReq;
import com.maruhxn.todomon.core.global.auth.checker.IsMyTodoOrAdmin;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.BadRequestException;
import com.maruhxn.todomon.core.global.error.exception.NotFoundException;
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

    private final MemberRepository memberRepository;
    private final TodoRepository todoRepository;
    private final RepeatInfoService repeatInfoService;
    private final TodoInstanceService todoInstanceService;
    private final RewardService rewardService;

    /**
     * Todo를 생성한다.
     *
     * @param memberId
     * @param req
     */
    public void create(Long memberId, CreateTodoReq req) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_MEMBER));

        Todo todo = req.toEntity(member);

        if (req.getRepeatInfoReqItem() != null) {
            RepeatInfo repeatInfo = repeatInfoService.createRepeatInfo(req.getRepeatInfoReqItem());
            todo.setRepeatInfo(repeatInfo); // Cascade.PERSIST에 의해 함께 저장됨
        }

        todoRepository.save(todo);

        if (todo.getRepeatInfo() != null) {
            todoInstanceService.generateAndSaveInstances(todo);
        }
    }

    @IsTodayTodoOrAdmin
    @IsMyTodoOrAdmin
    public void update(Long objectId, UpdateAndDeleteTodoQueryParams params, UpdateTodoReq req) {
        validateUpdateReq(req);

        if (params.getIsInstance()) {
            todoInstanceService.updateTodoInstance(objectId, params, req);
        } else {
            this.updateTodo(objectId, req);
        }
    }

    private static void validateUpdateReq(UpdateTodoReq req) {
        if (req.getContent() == null && req.getIsAllDay() == null && req.getRepeatInfoReqItem() == null) {
            throw new BadRequestException(ErrorCode.VALIDATION_ERROR, "수정할 데이터를 넘겨주세요");
        }
    }

    private void updateTodo(Long objectId, UpdateTodoReq req) {
        Todo findTodo = todoRepository.findById(objectId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_TODO));

        findTodo.update(req);

        if (req.getRepeatInfoReqItem() != null) {
            repeatInfoService.updateRepeatInfo(req, findTodo);
            todoInstanceService.generateAndSaveInstances(findTodo);
        }
    }

    /**
     * 반복 정보가 있는 경우 -> 해당 반복을 수행할 때마다 일관성 게이지가 오름, 보상은 그대로 + 반복 종료일까지 모든 투두를 수행했을 경우 "누적된" 보상의 지급
     * 반복 정보가 없는 단일 Todo의 경우 -> 단순 상태 업데이트 및 보상 지급
     *
     * @param objectId
     * @param memberId
     * @param isInstance
     * @param req
     */
    @IsTodayTodoOrAdmin
    @IsMyTodoOrAdmin
    public void updateStatusAndReward(Long objectId, boolean isInstance, Long memberId, UpdateTodoStatusReq req) {
        Member findMember = memberRepository.findMemberWithDiligenceUsingLock(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_MEMBER));

        if (isInstance) {
            todoInstanceService.doUpdateProcessForTodoInstance(objectId, req, findMember);
        } else {
            this.doUpdateProcessForSingleTodo(objectId, req, findMember);
        }
    }

    // 반복 정보가 없는 단일 todo의 경우 -> 단순 상태 업데이트 및 보상 지급
    private void doUpdateProcessForSingleTodo(Long objectId, UpdateTodoStatusReq req, Member findMember) {
        Todo findTodo = todoRepository.findById(objectId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_TODO));

        if (!findTodo.isDone() && req.getIsDone()) {
            findTodo.updateIsDone(true);
            rewardService.reward(findMember, 1);
        } else if (findTodo.isDone() && !req.getIsDone()) {
            findTodo.updateIsDone(false);
            rewardService.withdrawReward(findMember, 1);
        }
    }

    @IsMyTodoOrAdmin
    public void deleteTodo(Long objectId, UpdateAndDeleteTodoQueryParams params) {
        if (params.getIsInstance()) {
            Todo parentOfInstance = todoInstanceService.deleteTodoInstancesAndReturnParent(objectId, params);
            if (parentOfInstance.getTodoInstances().isEmpty()) todoRepository.delete(parentOfInstance);
        } else {
            this.deleteSingleTodo(objectId); // CASCADE로 인스턴스도 같이 삭제
        }
    }

    private void deleteSingleTodo(Long objectId) {
        Todo findTodo = todoRepository.findById(objectId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_TODO));
        todoRepository.delete(findTodo);
    }

}
