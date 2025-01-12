package com.maruhxn.todomon.core.domain.todo.application;

import com.maruhxn.todomon.core.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.todo.dao.TodoInstanceRepository;
import com.maruhxn.todomon.core.domain.todo.dao.TodoRepository;
import com.maruhxn.todomon.core.domain.todo.domain.RepeatInfo;
import com.maruhxn.todomon.core.domain.todo.domain.Todo;
import com.maruhxn.todomon.core.domain.todo.domain.TodoInstance;
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

import java.util.List;

import static com.maruhxn.todomon.core.global.common.Constants.*;

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
    private final TodoInstanceRepository todoInstanceRepository;
    private final TodoInstanceService todoInstanceService;

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
            TodoInstance todoInstance = todoInstanceRepository.findTodoInstanceWithTodo(objectId)
                    .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_TODO));

            if (req.getIsDone()) {
                if (!todoInstance.isDone()) {
                    todoInstance.updateIsDone(true);
                    rewardForInstance(todoInstance, findMember);
                }
            } else {
                if (todoInstance.isDone()) {
                    withdrawRewardForInstance(todoInstance, findMember);
                    todoInstance.updateIsDone(false);
                }
            }
        } else {
            Todo findTodo = todoRepository.findById(objectId)
                    .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_TODO));
            // 반복 정보가 없는 단일 todo의 경우 -> 단순 상태 업데이트 및 보상 지급
            if (!findTodo.isDone() && req.getIsDone()) {
                findTodo.updateIsDone(true);
                reward(findMember, 1);
            } else if (findTodo.isDone() && !req.getIsDone()) {
                findTodo.updateIsDone(false);
                withdrawReward(findMember, 1);
            }
        }
    }

    private void withdrawRewardForInstance(TodoInstance todoInstance, Member member) {
        withdrawReward(member, 1);
        List<TodoInstance> todoInstances = todoInstanceRepository.findAllByTodo_Id(todoInstance.getTodo().getId());
        if (checkAlreadyRewardedForAllCompleted(todoInstances)) { // 이미 모든 인스턴스가 완료되어 보상을 받았는지 확인
            todoInstance.getTodo().updateIsDone(false);
            withdrawReward(member, todoInstances.size());
        }
    }

    private boolean checkAlreadyRewardedForAllCompleted(List<TodoInstance> todoInstances) {
        for (TodoInstance todoInstance : todoInstances) {
            if (!todoInstance.isDone()) {
                return false;
            }
        }
        return true;
    }

    private void withdrawReward(Member member, int leverage) {
        member.addDailyAchievementCnt(-1);
        member.getDiligence().decreaseGauge(GAUGE_INCREASE_RATE * leverage);
        member.subtractScheduledReward((long) (REWARD_UNIT * leverage * REWARD_LEVERAGE_RATE));
    }

    // 단일 일정에 대한 보상 로직
    private void reward(Member member, int leverage) {
        // 일간 달성 수 1 증가
        if (leverage == 1) member.addDailyAchievementCnt(1);
        // 유저 일관성 게이지 업데이트
        member.getDiligence().increaseGauge(GAUGE_INCREASE_RATE * leverage);
        // 보상 지급
        member.addScheduledReward((long) (REWARD_UNIT * leverage * REWARD_LEVERAGE_RATE));
    }

    // 반복 일정에 대한 보상 로직
    private void rewardForInstance(TodoInstance todoInstance, Member member) {
        // 각 인스턴스가 수행되면 보상 지급
        reward(member, 1);

        if (todoInstance.getEndAt().equals(todoInstance.getTodo().getEndAt())) { // 마지막 인스턴스를 수행 완료 시
            List<TodoInstance> todoInstances = todoInstanceRepository.findAllByTodo_Id(todoInstance.getTodo().getId());
            // 모든 인스턴스 수행 완료 여부 확인
            if (checkIfAllRepeatsCompleted(todoInstances)) {
                todoInstance.getTodo().updateIsDone(true);
                reward(member, todoInstances.size());
            }
        }
    }

    // 반복 종료일까지 설정한 모든 todo를 수행했는지 체크
    private boolean checkIfAllRepeatsCompleted(List<TodoInstance> todoInstances) {
        int notCompletedInstancesSize = todoInstances.stream()
                .filter(instance -> !instance.isDone())
                .toList().size();
        return notCompletedInstancesSize == 0;
    }

    @IsMyTodoOrAdmin
    public void deleteTodo(Long objectId, UpdateAndDeleteTodoQueryParams params) {
        if (params.getIsInstance()) {
            TodoInstance todoInstance = todoInstanceRepository.findById(objectId)
                    .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_TODO));
            Todo todo = todoInstance.getTodo();
            List<TodoInstance> todoInstances = todo.getTodoInstances();

            switch (params.getTargetType()) {
                case THIS_TASK -> {
                    todoInstanceRepository.delete(todoInstance);
                    todoInstances.remove(todoInstance);
                }
                case ALL_TASKS -> {
                    todoInstanceRepository.deleteAllByTodo_Id(todo.getId());
                    todoInstances.clear();
                }
            }

            if (todoInstances.isEmpty()) {
                todoRepository.delete(todo);
            }
        } else {
            Todo findTodo = todoRepository.findById(objectId)
                    .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_TODO));
            todoRepository.delete(findTodo);
        }
    }

}
