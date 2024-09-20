package com.maruhxn.todomon.core.domain.todo.domain;

import com.maruhxn.todomon.core.domain.member.domain.Member;
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
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Todo extends BaseEntity {
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
    private boolean isAllDay = false;

    @Column(nullable = false, length = 7, columnDefinition = "varchar(7) default '#ffffff'")
    private String color = "#ffffff";

    @JoinColumn(name = "writer_id", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Member writer;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "repeat_info_id", referencedColumnName = "id")
    private RepeatInfo repeatInfo;

    @OneToMany(mappedBy = "todo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TodoInstance> todoInstances = new ArrayList<>();

    @Builder
    public Todo(String content, LocalDateTime startAt, LocalDateTime endAt, boolean isAllDay, String color, Member writer) {
        this.content = content;
        this.startAt = startAt;
        this.endAt = endAt;
        this.isAllDay = isAllDay;
        if (isAllDay) updateToAllDay();
        if (color != null) this.color = color;
        this.writer = writer;
    }

    public void updateToAllDay() {
        this.startAt = LocalDateTime.of(startAt.toLocalDate(), LocalDateTime.MIN.toLocalTime());
        this.endAt = LocalDateTime.of(endAt.toLocalDate(), LocalDateTime.MAX.toLocalTime());
    }

    /* 연관관계 메서드 */
    public void setRepeatInfo(RepeatInfo repeatInfo) {
        if (this.repeatInfo != null) {
            this.repeatInfo.setTodo(null);  // 기존 연관 관계 제거
        }

        this.repeatInfo = repeatInfo;

        if (repeatInfo != null) {
            repeatInfo.setTodo(this);  // 새로운 연관 관계 설정
        }
    }

    public void updateIsDone(boolean isDone) {
        this.isDone = isDone;
    }

    public void update(UpdateTodoReq req) {
        if (StringUtils.hasText(req.getContent())) this.content = req.getContent();
        if (req.getStartAt() != null) this.startAt = req.getStartAt();
        if (req.getEndAt() != null) this.endAt = req.getEndAt();
        if (req.getIsAllDay() != null) {
            this.isAllDay = req.getIsAllDay();
            if (this.isAllDay) updateToAllDay();
        }
        if (req.getColor() != null) {
            this.color = req.getColor();
        }
    }

    public void setTodoInstances(List<TodoInstance> instances) {
        this.todoInstances = instances;
    }

    public void updateEndAtTemporally() {
        this.endAt = LocalDateTime.of(this.startAt.toLocalDate(), this.endAt.toLocalTime());
    }
}
