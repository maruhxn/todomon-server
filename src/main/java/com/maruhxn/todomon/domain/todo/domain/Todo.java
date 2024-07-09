package com.maruhxn.todomon.domain.todo.domain;

import com.maruhxn.todomon.domain.member.domain.Member;
import com.maruhxn.todomon.domain.todo.dto.request.UpdateTodoReq;
import com.maruhxn.todomon.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

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
    @ColumnDefault("1")
    private boolean isAllDay = false;

    @JoinColumn(name = "writer_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Member writer;

    @OneToOne(mappedBy = "todo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private RepeatInfo repeatInfo;

    @Builder
    public Todo(String content, LocalDateTime startAt, LocalDateTime endAt, boolean isAllDay, Member writer) {
        this.content = content;
        this.startAt = startAt;
        this.endAt = endAt;
        this.isAllDay = isAllDay;
        if (isAllDay) updateToAllDay();
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
        if (req.getEndAt() != null) this.startAt = req.getEndAt();
        if (req.getIsAllDay() != null) {
            this.isAllDay = req.getIsAllDay();
            updateToAllDay();
        }
    }
}
