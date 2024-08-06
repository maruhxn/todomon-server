package com.maruhxn.todomon.domain.todo.application;

import com.maruhxn.todomon.domain.member.domain.Member;
import com.maruhxn.todomon.domain.todo.dao.RepeatInfoRepository;
import com.maruhxn.todomon.domain.todo.dao.TodoInstanceRepository;
import com.maruhxn.todomon.domain.todo.dao.TodoRepository;
import com.maruhxn.todomon.domain.todo.domain.RepeatInfo;
import com.maruhxn.todomon.domain.todo.domain.Todo;
import com.maruhxn.todomon.domain.todo.domain.TodoInstance;
import com.maruhxn.todomon.domain.todo.dto.request.*;
import com.maruhxn.todomon.global.error.ErrorCode;
import com.maruhxn.todomon.global.error.exception.BadRequestException;
import com.maruhxn.todomon.global.error.exception.NotFoundException;
import com.maruhxn.todomon.global.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.maruhxn.todomon.global.common.Constants.*;

/**
 * create, update 시 입력받는 startAt, endAt에 대한 유효성 검증 필요
 */
@Service
@Transactional
@RequiredArgsConstructor
public class TodoService {

    private final TodoRepository todoRepository;
    private final RepeatInfoRepository repeatInfoRepository;
    private final TodoInstanceRepository todoInstanceRepository;

    /**
     * Todo를 생성한다.
     *
     * @param member
     * @param req
     */
    public void create(Member member, CreateTodoReq req) {
        Todo todo = req.toEntity(member);
        RepeatInfo repeatInfo = null;
        if (req.getRepeatInfoItem() != null) {
            repeatInfo = createAndSetRepeatInfo(req.getRepeatInfoItem(), todo);
        }

        todoRepository.save(todo);

        if (repeatInfo != null) {
            createTodoInstances(todo);
        }
    }

    private RepeatInfo createAndSetRepeatInfo(RepeatInfoItem repeatInfoItem, Todo todo) {
        RepeatInfo repeatInfo = repeatInfoItem.toEntity();
        todo.setRepeatInfo(repeatInfo);
        repeatInfoRepository.save(repeatInfo);
        return repeatInfo;
    }

    private void createTodoInstances(Todo todo) {
        RepeatInfo repeatInfo = todo.getRepeatInfo();
        LocalDateTime startAt = todo.getStartAt();
        LocalDateTime endAt = todo.getEndAt();

        List<TodoInstance> instances = new ArrayList<>();

        switch (repeatInfo.getFrequency()) {
            case DAILY -> instances.addAll(generateDailyInstances(todo, startAt, endAt, repeatInfo));
            case WEEKLY -> instances.addAll(generateWeeklyInstances(todo, startAt, endAt, repeatInfo));
            case MONTHLY -> instances.addAll(generateMonthlyInstances(todo, startAt, endAt, repeatInfo));
        }

        if (instances.size() > 1) { // 최소 반복 횟수를 넘지 못하면 인스턴스를 생성하지 않고, 단일 투두로 처리
            todo.setTodoInstances(instances);
            todoInstanceRepository.saveAll(instances);
            LocalDateTime repeatStartAt = instances.get(0).getStartAt();
            LocalDateTime repeatEndAt = instances.get(instances.size() - 1).getEndAt();
            todo.update(UpdateTodoReq.builder()
                    .startAt(repeatStartAt)
                    .endAt(repeatEndAt)
                    .build());
        }
    }

    private List<TodoInstance> generateMonthlyInstances(Todo todo, LocalDateTime startAt, LocalDateTime endAt, RepeatInfo repeatInfo) {
        List<TodoInstance> instances = new ArrayList<>();
        Integer dayOfMonth = repeatInfo.getByMonthDay();
        LocalDateTime currentStart = startAt;
        LocalDateTime currentEnd = endAt;

        currentStart = adjustDayOfMonth(currentStart, dayOfMonth, repeatInfo.getInterval());
        currentEnd = adjustDayOfMonth(currentEnd, dayOfMonth, repeatInfo.getInterval());

        if (startAt.isAfter(currentStart)) {
            currentStart = currentStart.plusMonths(repeatInfo.getInterval());
            currentEnd = currentEnd.plusMonths(repeatInfo.getInterval());
        }

        while (shouldGenerateMoreInstances(currentStart, repeatInfo, instances.size())) {
            currentStart = adjustDayOfMonth(currentStart, dayOfMonth, repeatInfo.getInterval());
            currentEnd = adjustDayOfMonth(currentEnd, dayOfMonth, repeatInfo.getInterval());
            instances.add(TodoInstance.of(todo, currentStart, currentEnd));
            currentStart = currentStart.plusMonths(repeatInfo.getInterval());
            currentEnd = currentEnd.plusMonths(repeatInfo.getInterval());
        }

        return instances;
    }

    private LocalDateTime adjustDayOfMonth(LocalDateTime dateTime, int dayOfMonth, int interval) {
        int maxDayOfMonth = dateTime.getMonth().length(dateTime.toLocalDate().isLeapYear());
        if (dayOfMonth > maxDayOfMonth) {
            return dateTime.plusMonths(interval).withDayOfMonth(dayOfMonth);
        }
        return dateTime.withDayOfMonth(dayOfMonth);
    }

    private List<TodoInstance> generateWeeklyInstances(Todo todo, LocalDateTime startAt, LocalDateTime endAt, RepeatInfo repeatInfo) {
        List<TodoInstance> instances = new ArrayList<>();
        LocalDateTime currentStart = startAt;
        LocalDateTime currentEnd = endAt;

        // 주어진 요일 목록 파싱
        List<DayOfWeek> byDays = Arrays.stream(repeatInfo.getByDay().split(","))
                .map(TimeUtil::convertToDayOfWeek)
                .toList();

        while (shouldGenerateMoreInstances(currentStart, repeatInfo, instances.size())) {
            if (byDays.contains(currentStart.getDayOfWeek())) { // 현재 날짜가 byDays에 포함되는지 확인
                instances.add(TodoInstance.of(todo, currentStart, currentEnd)); // 포함된다면 인스턴스 생성
            }
            currentStart = currentStart.plusDays(1);
            currentEnd = currentEnd.plusDays(1);
            if (currentStart.getDayOfWeek() == DayOfWeek.MONDAY) {
                currentStart = currentStart.plusWeeks(repeatInfo.getInterval() - 1);
                currentEnd = currentEnd.plusDays(repeatInfo.getInterval() - 1);
            }

        }

        return instances;
    }

    private List<TodoInstance> generateDailyInstances(Todo todo, LocalDateTime startAt, LocalDateTime endAt, RepeatInfo repeatInfo) {
        List<TodoInstance> instances = new ArrayList<>();
        LocalDateTime currentStart = startAt;
        LocalDateTime currentEnd = endAt;

        while (shouldGenerateMoreInstances(currentStart, repeatInfo, instances.size())) {
            instances.add(TodoInstance.of(todo, currentStart, currentEnd));
            currentStart = currentStart.plusDays(repeatInfo.getInterval());
            currentEnd = currentEnd.plusDays(repeatInfo.getInterval());
        }

        return instances;
    }


    // 현재 시간이 규칙에 의해 정의된 종료 시점이나 반복 횟수 조건을 초과하지 않았는지 확인
    private boolean shouldGenerateMoreInstances(LocalDateTime currentStart, RepeatInfo repeatInfo, int size) {
        if (repeatInfo.getUntil() != null && currentStart.isAfter(repeatInfo.getUntil().plusDays(1).atStartOfDay()))
            return false;
        if (repeatInfo.getCount() != null && size >= repeatInfo.getCount()) return false;
        return true;
    }

    public void update(Long objectId, UpdateAndDeleteTodoQueryParams params, UpdateTodoReq req) {
        validateUpdateReq(req);

        if (params.getIsInstance()) {
            TodoInstance todoInstance = todoInstanceRepository.findById(objectId)
                    .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_TODO));
            switch (params.getTargetType()) {
                case THIS_TASK -> {
                    // 반복 정보 수정은 ALL_TASKS 타입만 가능
                    if (req.getRepeatInfoItem() != null) {
                        throw new BadRequestException(ErrorCode.BAD_REQUEST, "전체 인스턴스에 대해서만 반복 정보 수정이 가능합니다.");
                    }

                    if (req.getEndAt() == null && todoInstance.getEndAt().isBefore(req.getStartAt())
                            || req.getStartAt() == null && todoInstance.getStartAt().isAfter(req.getEndAt())
                    ) {
                        throw new BadRequestException(ErrorCode.BAD_REQUEST, "시작 시각은 종료 시각보다 이전이어야 합니다.");
                    }

                    todoInstance.update(req);
                }
                case ALL_TASKS -> {
                    // 시간 정보 수정은 THIS_TASK 타입만 가능
                    if (req.getStartAt() != null || req.getEndAt() != null) {
                        throw new BadRequestException(ErrorCode.BAD_REQUEST, "시간 정보 수정은 단일 인스턴스에 대해서만 수정 가능합니다.");
                    }

                    Todo todo = todoInstance.getTodo();
                    todo.update(req); // todo 먼저 업데이트

                    if (req.getRepeatInfoItem() != null) {
                        RepeatInfo oldRepeatInfo = todo.getRepeatInfo();
                        if (oldRepeatInfo != null) {
                            todo.setRepeatInfo(null);
                            todo.setTodoInstances(null);
                            repeatInfoRepository.delete(oldRepeatInfo);
                            todoInstanceRepository.deleteAllByTodo_Id(todo.getId());
                        }
                        todo.updateEndAtTemporally();
                        createAndSetRepeatInfo(req.getRepeatInfoItem(), todo);
                        createTodoInstances(todo);
                    } else {
                        List<TodoInstance> todoInstances = todoInstanceRepository.findAllByTodo_Id(todoInstance.getTodo().getId());
                        todoInstances.forEach(instance -> instance.update(req));
                    }
                }
            }
        } else {
            Todo findTodo = todoRepository.findById(objectId)
                    .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_TODO));

            findTodo.update(req);

            if (req.getRepeatInfoItem() != null) {
                createAndSetRepeatInfo(req.getRepeatInfoItem(), findTodo);
                createTodoInstances(findTodo);
            }
        }


    }

    private static void validateUpdateReq(UpdateTodoReq req) {
        if (req.getContent() == null && req.getIsAllDay() == null && req.getRepeatInfoItem() == null) {
            throw new BadRequestException(ErrorCode.VALIDATION_ERROR, "수정할 데이터를 넘겨주세요");
        }
    }

    /**
     * 반복 정보가 있는 경우 -> 해당 반복을 수행할 때마다 일관성 게이지가 오름, 보상은 그대로 + 반복 종료일까지 모든 투두를 수행했을 경우 "누적된" 보상의 지급
     * 반복 정보가 없는 단일 Todo의 경우 -> 단순 상태 업데이트 및 보상 지급
     *
     * @param objectId
     * @param member
     * @param isInstance
     * @param req
     */
    public void updateStatusAndReward(Long objectId, Member member, boolean isInstance, UpdateTodoStatusReq req) {
        if (isInstance) {
            TodoInstance todoInstance = todoInstanceRepository.findById(objectId)
                    .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_TODO));
            if (req.getIsDone()) {
                todoInstance.updateIsDone(true);
                rewardForInstance(todoInstance, member);
            } else {
                withdrawRewardForInstance(todoInstance, member);
                todoInstance.updateIsDone(false);
            }
        } else {
            Todo findTodo = todoRepository.findById(objectId)
                    .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_TODO));
            // 반복 정보가 없는 단일 todo의 경우 -> 단순 상태 업데이트 및 보상 지급
            findTodo.updateIsDone(req.getIsDone());
            if (req.getIsDone()) {
                reward(member, 1);
            } else {
                withdrawReward(member, 1);
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
        member.addDailyAchievementCnt(1);
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
                    todoInstances.clear();
                    todoInstanceRepository.deleteAllByTodo_Id(todo.getId());
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
