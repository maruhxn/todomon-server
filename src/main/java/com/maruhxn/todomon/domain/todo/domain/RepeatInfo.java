package com.maruhxn.todomon.domain.todo.domain;

import com.maruhxn.todomon.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RepeatInfo extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(length = 8, nullable = false)
    private Frequency frequency;

    @Column(nullable = false, name = "repeat_interval") // interval은 일부 데이터베이스에서 예약어로 취급
    private Integer interval = 1; // 반복 일정의 빈도 = 반복되는 이벤트가 얼마나 자주 발생하는지를 나타냄

    private LocalDate until; // 반복이 끝나는 날짜 지정. 이 날짜까지 이벤트가 반복 -> count와 공존 불가능

    @Column(length = 20)
    private String byDay;
    private Integer byMonthDay;
    private Integer count; // 반복 일정의 총 발생 횟수 = 반복되는 이벤트가 몇 번 발생해야 하는지를 나타냄

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", referencedColumnName = "id")
    private Todo todo;

    @Builder
    public RepeatInfo(Frequency frequency, Integer interval, LocalDate until, String byDay, Integer byMonthDay, Integer count) {
        this.frequency = frequency;
        this.interval = interval;
        this.until = until;
        this.byDay = byDay;
        this.byMonthDay = byMonthDay;
        this.count = count;
    }

    public void subtractCount() {
        this.count -= 1;
    }

    /* 연관관계 메서드 */
    public void setTodo(Todo todo) {
        this.todo = todo;
    }
}
