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

    private LocalDateTime originalStartAt; // 반복 일정의 원래 시작 시간을 추적하는 데 사용
    private LocalDateTime originalEndAt;

    @Builder
    public TodoInstance(Todo todo, LocalDateTime startAt, LocalDateTime endAt, boolean isAllDay, LocalDateTime originalStartAt, LocalDateTime originalEndAt) {
        this.todo = todo;
        this.startAt = startAt;
        this.endAt = endAt;
        this.isAllDay = isAllDay;
        this.originalStartAt = originalStartAt;
        this.originalEndAt = originalEndAt;
    }

    public static TodoInstance of(Todo todo, LocalDateTime startAt, LocalDateTime endAt) {
        return TodoInstance.builder()
                .todo(todo)
                .startAt(startAt)
                .endAt(endAt)
                .isAllDay(todo.isAllDay())
                .originalStartAt(todo.getStartAt())
                .originalEndAt(todo.getEndAt())
                .build();
    }
}
