package com.maruhxn.todomon.domain.todo.application;

import com.maruhxn.todomon.domain.member.domain.Member;
import com.maruhxn.todomon.domain.todo.dao.RepeatInfoRepository;
import com.maruhxn.todomon.domain.todo.dao.TodoInstanceRepository;
import com.maruhxn.todomon.domain.todo.dao.TodoRepository;
import com.maruhxn.todomon.domain.todo.domain.RepeatInfo;
import com.maruhxn.todomon.domain.todo.domain.Todo;
import com.maruhxn.todomon.domain.todo.domain.TodoInstance;
import com.maruhxn.todomon.domain.todo.dto.request.CreateTodoReq;
import com.maruhxn.todomon.domain.todo.dto.request.UpdateTodoReq;
import com.maruhxn.todomon.domain.todo.dto.request.UpdateTodoStatusReq;
import com.maruhxn.todomon.global.error.ErrorCode;
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
     * <p>
     * + 4일짜리 Todo는 해당 Todo를 종료 일자까지 매일 반복하는 것과 동일
     *
     * @param member
     * @param req
     */
    public void create(Member member, CreateTodoReq req) {
        Todo todo = req.toEntity(member);
        RepeatInfo repeatInfo = null;
        if (req.getRepeatInfoItem() != null) {
            repeatInfo = req.getRepeatInfoItem().toEntity();
            todo.setRepeatInfo(repeatInfo);
            repeatInfoRepository.save(repeatInfo);
        }

        todoRepository.save(todo);

        if (repeatInfo != null) {
            createTodoInstances(todo);
        }
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

        todoInstanceRepository.saveAll(instances);
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

    // Todo 정보를 업데이트한다. 새로운 반복 정보가 들어오면 기존 반복 정보를 제거한 후, 덮어씌운다.
    public void update(Long todoId, UpdateTodoReq req) {
        Todo findTodo = todoRepository.findById(todoId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_TODO));
        findTodo.update(req);
        if (req.getRepeatInfoItem() != null) {
            RepeatInfo oldRepeatInfo = findTodo.getRepeatInfo();
            if (oldRepeatInfo != null) {
                findTodo.setRepeatInfo(null);
                repeatInfoRepository.delete(oldRepeatInfo);
                todoInstanceRepository.deleteAllByTodo_Id(todoId);
            }
            RepeatInfo repeatInfo = req.getRepeatInfoItem().toEntity();
            repeatInfoRepository.save(repeatInfo);
            findTodo.setRepeatInfo(repeatInfo);
            createTodoInstances(findTodo);
        }
    }

//    /**
//     * 반복 정보가 있는 경우 -> 해당 반복을 수행할 때마다 일관성 게이지가 오름, 보상은 그대로 + 반복 종료일까지 모든 투두를 수행했을 경우 "누적된" 보상의 지급
//     * 반복 정보가 없는 단일 Todo의 경우 -> 단순 상태 업데이트 및 보상 지급
//     *
//     * @param todoId
//     * @param req
//     */
//    public void updateStatusAndReward(Long todoId, UpdateTodoStatusReq req) {
//        Todo findTodo = todoRepository.findById(todoId)
//                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_TODO));
//
//        if (findTodo.getRepeatInfo() == null) {
//            // 반복 정보가 없는 단일 todo의 경우 -> 단순 상태 업데이트 및 보상 지급
//            findTodo.updateIsDone(req.isDone());
//            rewardUserForSingleTodoCompletion(findTodo);
//        } else {
//            updateRepeatInfoAndRewardUser(findTodo);
//        }
//    }
//
//    /**
//     * 단일 Todo의 경우 보상 로직
//     *
//     * @param todo
//     */
//    private void rewardUserForSingleTodoCompletion(Todo todo) {
//        // 유저 일관성 게이지 업데이트
//        // 보상 지급
//    }
//
//    private void updateRepeatInfoAndRewardUser(Todo todo) {
//        RepeatInfo repeatInfo = todo.getRepeatInfo();
//        boolean allRepeatsCompleted = checkIfAllRepeatsCompleted(todo, repeatInfo);
//
//        if (allRepeatsCompleted) {
//            rewardUserForAllRepeatsCompletion(todo, repeatInfo);
//        }
//    }
//
//    // 반복 종료일까지 설정한 모든 todo를 수행했는지 체크
//    private boolean checkIfAllRepeatsCompleted(Todo todo, RepeatInfo repeatInfo) {
//        // Logic to check if all repeats of the todo until endDate are completed
//        // Example: Query database to count completed repeats
//        // Compare with expected number of repeats based on repeatType and endDate
//        return false; // Placeholder logic
//    }
//
//    // 반복 설정된 투두를 모두 수행했을 시 추가 보상 지급 로직
//    private void rewardUserForAllRepeatsCompletion(Todo todo, RepeatInfo repeatInfo) {
//
//    }
//
//
//    public void deleteTodo(Long todoId) {
//
//    }

}
