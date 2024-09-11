package com.maruhxn.todomon.core.domain.todo.domain;

import com.maruhxn.todomon.core.global.common.BaseEntity;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TodoAchievementHistory extends BaseEntity {

    private Long memberId;

    private Long cnt;

    private LocalDate date;

    @Builder
    public TodoAchievementHistory(Long memberId, Long cnt, LocalDate date) {
        this.memberId = memberId;
        this.cnt = cnt;
        this.date = date;
    }
}
