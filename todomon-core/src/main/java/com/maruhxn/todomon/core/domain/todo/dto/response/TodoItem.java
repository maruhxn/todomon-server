package com.maruhxn.todomon.core.domain.todo.dto.response;

import com.maruhxn.todomon.core.domain.todo.domain.Todo;
import com.maruhxn.todomon.core.domain.todo.domain.TodoInstance;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * {
 * "todoId": "todoId",
 * "parentId": "parentId", // 반복 일정인 경우, 같은 todo로부터 생성된 instance인지 확인하기 위함
 * "content": "내용",
 * "isAllDay": false,
 * "isDone": false,
 * "startAt": "20240707~",
 * "endAt": "20240707~",
 * }
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TodoItem {
    private Long todoId;
    private Long parentId;
    private String content;
    private String color;
    private boolean isAllDay;
    private boolean isDone;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private RepeatInfoItem repeatInfoItem;

    @Builder
    public TodoItem(Long todoId, Long parentId, String content, String color, boolean isAllDay, boolean isDone, LocalDateTime startAt, LocalDateTime endAt, RepeatInfoItem repeatInfoItem) {
        this.todoId = todoId;
        this.parentId = parentId;
        this.content = content;
        this.color = color;
        this.isAllDay = isAllDay;
        this.isDone = isDone;
        this.startAt = startAt;
        this.endAt = endAt;
        this.repeatInfoItem = repeatInfoItem;
    }

    public static TodoItem from(Todo todo) {
        return TodoItem.builder()
                .todoId(todo.getId())
                .parentId(null)
                .content(todo.getContent())
                .color(todo.getColor())
                .startAt(todo.getStartAt())
                .endAt(todo.getEndAt())
                .isAllDay(todo.isAllDay())
                .isDone(todo.isDone())
                .repeatInfoItem(null)
                .build();
    }

    public static TodoItem from(TodoInstance todoInstance) {
        return TodoItem.builder()
                .todoId(todoInstance.getId())
                .parentId(todoInstance.getTodo().getId())
                .content(todoInstance.getContent())
                .color(todoInstance.getColor())
                .startAt(todoInstance.getStartAt())
                .endAt(todoInstance.getEndAt())
                .isAllDay(todoInstance.isAllDay())
                .isDone(todoInstance.isDone())
                .repeatInfoItem(RepeatInfoItem.from(todoInstance.getTodo().getRepeatInfo()))
                .build();
    }
}
