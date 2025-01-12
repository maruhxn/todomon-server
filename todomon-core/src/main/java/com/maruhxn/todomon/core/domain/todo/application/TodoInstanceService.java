package com.maruhxn.todomon.core.domain.todo.application;

import com.maruhxn.todomon.core.domain.todo.application.strategy.RepeatInfoStrategy;
import com.maruhxn.todomon.core.domain.todo.dao.TodoInstanceRepository;
import com.maruhxn.todomon.core.domain.todo.domain.RepeatInfo;
import com.maruhxn.todomon.core.domain.todo.domain.Todo;
import com.maruhxn.todomon.core.domain.todo.domain.TodoInstance;
import com.maruhxn.todomon.core.domain.todo.dto.request.UpdateAndDeleteTodoQueryParams;
import com.maruhxn.todomon.core.domain.todo.dto.request.UpdateTodoReq;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.BadRequestException;
import com.maruhxn.todomon.core.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TodoInstanceService {

    private final TodoInstanceRepository todoInstanceRepository;
    private final RepeatInfoService repeatInfoService;
    private final RepeatInfoStrategyFactory strategyFactory;

    public void generateAndSaveInstances(Todo todo) {
        RepeatInfo repeatInfo = todo.getRepeatInfo();
        RepeatInfoStrategy strategy = strategyFactory.getStrategy(repeatInfo.getFrequency());
        List<TodoInstance> instances = strategy.generateInstances(todo);

        if (!instances.isEmpty()) {
            todo.setTodoInstances(instances);
            todoInstanceRepository.saveAll(instances);
            this.updateTodoDateRange(todo, instances);
        }
    }

    private void updateTodoDateRange(Todo todo, List<TodoInstance> instances) {
        LocalDateTime repeatStartAt = instances.get(0).getStartAt();
        LocalDateTime repeatEndAt = instances.get(instances.size() - 1).getEndAt();
        todo.update(UpdateTodoReq.builder()
                .startAt(repeatStartAt)
                .endAt(repeatEndAt)
                .build());
    }

    public void updateTodoInstance(Long objectId, UpdateAndDeleteTodoQueryParams params, UpdateTodoReq req) {
        TodoInstance todoInstance = todoInstanceRepository.findTodoInstanceWithTodo(objectId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_TODO));

        switch (params.getTargetType()) {
            case THIS_TASK -> this.updateSingleInstance(req, todoInstance);
            case ALL_TASKS -> this.updateAllInstances(req, todoInstance);
            default -> throw new BadRequestException(ErrorCode.BAD_REQUEST, "잘못된 type입니다.");
        }
    }

    // 시간 정보 수정은 THIS_TASK 타입만 가능
    private void updateSingleInstance(UpdateTodoReq req, TodoInstance todoInstance) {
        // 반복 정보 수정은 ALL_TASKS 타입만 가능
        if (req.getRepeatInfoReqItem() != null)
            throw new BadRequestException(ErrorCode.BAD_REQUEST, "전체 인스턴스에 대해서만 반복 정보 수정이 가능합니다.");

        this.validateInstanceTime(req, todoInstance);

        todoInstance.update(req);
    }

    private void validateInstanceTime(UpdateTodoReq req, TodoInstance todoInstance) {
        if (req.getEndAt() == null && todoInstance.getEndAt().isBefore(req.getStartAt())
                || req.getStartAt() == null && todoInstance.getStartAt().isAfter(req.getEndAt())
        ) {
            throw new BadRequestException(ErrorCode.BAD_REQUEST, "시작 시각은 종료 시각보다 이전이어야 합니다.");
        }
    }

    private void updateAllInstances(UpdateTodoReq req, TodoInstance todoInstance) {
        if (req.getStartAt() != null || req.getEndAt() != null) {
            throw new BadRequestException(ErrorCode.BAD_REQUEST, "시간 정보 수정은 단일 인스턴스에 대해서만 수정 가능합니다.");
        }

        Todo todo = todoInstance.getTodo();
        todo.update(req); // todo 먼저 업데이트
        todoInstance.update(req);

        if (req.getRepeatInfoReqItem() != null) {
            this.updateAllWithNewRepeatInfo(req, todo);
        } else {
            this.updateAllWithoutNewRepeatInfo(req, todoInstance);
        }
    }

    private void updateAllWithNewRepeatInfo(UpdateTodoReq req, Todo todo) {
        repeatInfoService.removeOldRepeatInfo(todo);
        todoInstanceRepository.deleteAllByTodo_Id(todo.getId()); // 삭제할 대상이 없어도 예외 X
        todo.updateEndAtTemporally();
        repeatInfoService.updateRepeatInfo(req, todo);
        this.generateAndSaveInstances(todo);
    }

    private void updateAllWithoutNewRepeatInfo(UpdateTodoReq req, TodoInstance todoInstance) {
        todoInstanceRepository
                .findAllByTodo_Id(todoInstance.getTodo().getId())
                .forEach(instance -> instance.update(req));
    }

}
