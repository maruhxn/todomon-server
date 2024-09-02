package com.maruhxn.todomon.core.domain.todo.domain;

import com.maruhxn.todomon.core.domain.todo.dto.request.UpdateTodoReq;
import com.maruhxn.todomon.core.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TodoInstance extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "todo_id", nullable = false, referencedColumnName = "id")
    private Todo todo;

    @Column(length = 50, nullable = false)
    private String content;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private LocalDateTime endAt;

    @Column(nullable = false)
    @ColumnDefault("0")
    private boolean isDone = false;

    @Column(nullable = false)
    private String color;

    @Column(nullable = false)
    @ColumnDefault("1")
    private boolean isAllDay = false;

    @Builder
    public TodoInstance(Todo todo, String content, LocalDateTime startAt, LocalDateTime endAt, boolean isAllDay, String color) {
        this.todo = todo;
        this.content = content;
        this.startAt = startAt;
        this.endAt = endAt;
        this.isAllDay = isAllDay;
        this.color = color;
    }

    public static TodoInstance of(Todo todo, LocalDateTime startAt, LocalDateTime endAt) {
        return TodoInstance.builder()
                .todo(todo)
                .content(todo.getContent())
                .startAt(startAt)
                .endAt(endAt)
                .isAllDay(todo.isAllDay())
                .color(todo.getColor())
                .build();
    }

    public void updateIsDone(boolean isDone) {
        this.isDone = isDone;
    }

    public void update(UpdateTodoReq req) {
        if (StringUtils.hasText(req.getContent())) this.content = req.getContent();

        if (req.getStartAt() != null) {
            this.startAt = req.getStartAt();
        }

        if (req.getEndAt() != null) {
            this.endAt = req.getEndAt();
        }

        if (req.getIsAllDay() != null) {
            this.isAllDay = req.getIsAllDay();
            if (this.isAllDay) updateToAllDay();
        }

        if (req.getColor() != null) {
            this.color = req.getColor();
        }
    }

    public void updateToAllDay() {
        this.startAt = LocalDateTime.of(startAt.toLocalDate(), LocalDateTime.MIN.toLocalTime());
        this.endAt = LocalDateTime.of(endAt.toLocalDate(), LocalDateTime.MAX.toLocalTime());
    }
}
