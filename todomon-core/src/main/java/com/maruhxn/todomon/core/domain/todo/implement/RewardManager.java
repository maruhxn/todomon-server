package com.maruhxn.todomon.core.domain.todo.implement;

import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.todo.dao.TodoInstanceRepository;
import com.maruhxn.todomon.core.domain.todo.domain.TodoInstance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.maruhxn.todomon.core.global.common.Constants.REWARD_LEVERAGE_RATE;
import static com.maruhxn.todomon.core.global.common.Constants.REWARD_UNIT;

@Slf4j
@Component
@RequiredArgsConstructor
public class RewardManager {
    private final TodoInstanceRepository todoInstanceRepository;

    // 단일 일정에 대한 보상 로직
    public void reward(Member member, int todoCnt) {
        log.info("투두 달성 보상 지급 === 유저 아이디: {}, 개수: {}", member.getId(), todoCnt);
        // 일간 달성 수 1 증가
        if (todoCnt == 1) member.addDailyAchievementCnt(1);
        // 유저 일관성 게이지 업데이트
        member.getDiligence().increaseGaugeByTodoCnt(todoCnt);
        // 보상 지급
        member.addScheduledReward((int) (REWARD_UNIT * todoCnt * REWARD_LEVERAGE_RATE));
    }

    public void withdrawReward(Member member, int todoCnt) {
        log.info("투두 달성 철회 === 유저 아이디: {}, 개수: {}", member.getId(), todoCnt);
        member.addDailyAchievementCnt(-1);
        member.getDiligence().decreaseGaugeByTodoCnt(todoCnt);
        member.subtractScheduledReward((int) (REWARD_UNIT * todoCnt * REWARD_LEVERAGE_RATE));
    }

    // 반복 일정에 대한 보상 로직
    public void rewardForInstance(TodoInstance todoInstance, Member member) {
        // 각 인스턴스가 수행되면 보상 지급
        this.reward(member, 1);

        if (todoInstance.getEndAt().equals(todoInstance.getTodo().getEndAt())) { // 마지막 인스턴스를 수행 완료 시
            log.info("모든 인스턴스 달성 성공 === 유저 아이디: {}, 투두 아이디: {}", member.getId(), todoInstance.getTodo().getId());
            List<TodoInstance> todoInstances = todoInstanceRepository.findAllByTodo_Id(todoInstance.getTodo().getId());
            // 모든 인스턴스 수행 완료 여부 확인
            if (checkIfAllRepeatsCompleted(todoInstances)) {
                todoInstance.getTodo().updateIsDone(true);
                this.reward(member, todoInstances.size());
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

    public void withdrawRewardForInstance(TodoInstance todoInstance, Member member) {
        log.info("투두 인스턴스 달성 철회 === 유저 아이디: {}, 인스턴스 아이디: {}", member.getId(), todoInstance.getId());
        this.withdrawReward(member, 1);
        List<TodoInstance> todoInstances = todoInstanceRepository.findAllByTodo_Id(todoInstance.getTodo().getId());
        if (this.checkAlreadyRewardedForAllCompleted(todoInstances)) { // 이미 모든 인스턴스가 완료되어 보상을 받았는지 확인
            todoInstance.getTodo().updateIsDone(false);
            this.withdrawReward(member, todoInstances.size());
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
}
