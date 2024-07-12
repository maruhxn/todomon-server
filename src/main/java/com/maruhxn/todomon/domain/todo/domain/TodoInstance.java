package com.maruhxn.todomon.domain.todo.domain;

import com.maruhxn.todomon.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TodoInstance extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "todo_id", nullable = false, referencedColumnName = "id")
    private Todo todo;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private LocalDateTime endAt;

    @Column(nullable = false)
    @ColumnDefault("0")
    private boolean isDone = false;

    @Column(nullable = false)
    @ColumnDefault("1")
    private boolean isAllDay = false;

    @Builder
    public TodoInstance(Todo todo, LocalDateTime startAt, LocalDateTime endAt, boolean isAllDay) {
        this.todo = todo;
        this.startAt = startAt;
        this.endAt = endAt;
        this.isAllDay = isAllDay;
    }

    public static TodoInstance of(Todo todo, LocalDateTime startAt, LocalDateTime endAt) {
        return TodoInstance.builder()
                .todo(todo)
                .startAt(startAt)
                .endAt(endAt)
                .isAllDay(todo.isAllDay())
                .build();
    }

    public void updateIsDone(boolean isDone) {
        this.isDone = isDone;
    }
}
