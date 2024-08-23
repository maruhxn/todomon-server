package com.maruhxn.todomon.domain.todo.dto.response;

import com.maruhxn.todomon.domain.todo.domain.Frequency;
import com.maruhxn.todomon.domain.todo.domain.RepeatInfo;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RepeatInfoItem {
    private Frequency frequency;
    private int interval;
    private String byDay;
    private Integer byMonthDay;
    private LocalDate until;
    private Integer count;

    @Builder
    public RepeatInfoItem(Frequency frequency, int interval, String byDay, Integer byMonthDay, LocalDate until, Integer count) {
        Assert.isTrue(until == null || count == null, "반복 종료 날짜와 반복 횟수는 함께 입력할 수 없습니다.");
        this.frequency = frequency;
        this.interval = interval;
        this.byDay = byDay;
        this.byMonthDay = byMonthDay;
        this.until = until;
        this.count = count;
    }

    public static RepeatInfoItem from(RepeatInfo repeatInfo) {
        return RepeatInfoItem.builder()
                .frequency(repeatInfo.getFrequency())
                .interval(repeatInfo.getInterval())
                .byDay(repeatInfo.getByDay())
                .byMonthDay(repeatInfo.getByMonthDay())
                .until(repeatInfo.getUntil())
                .count(repeatInfo.getCount())
                .build();
    }
}