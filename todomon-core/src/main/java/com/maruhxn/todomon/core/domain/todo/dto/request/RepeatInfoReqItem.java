package com.maruhxn.todomon.core.domain.todo.dto.request;

import com.maruhxn.todomon.core.domain.todo.domain.Frequency;
import com.maruhxn.todomon.core.domain.todo.domain.RepeatInfo;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RepeatInfoReqItem {
    @NotNull(message = "반복 단위를 입력해주세요.")
    private Frequency frequency;
    private int interval;
    @Size(max = 30, message = "최대 30글자입니다.")
    private String byDay;
    private Integer byMonthDay;
    private LocalDate until;
    @Min(value = 2, message = "최소 반복 횟수는 2번입니다.")
    private Integer count;

    @Builder
    public RepeatInfoReqItem(Frequency frequency, int interval, String byDay, Integer byMonthDay, LocalDate until, Integer count) {
        Assert.isTrue(until == null || count == null, "반복 종료 날짜와 반복 횟수는 함께 입력할 수 없습니다.");
        this.frequency = frequency;
        this.interval = interval;
        this.byDay = byDay;
        this.byMonthDay = byMonthDay;
        this.until = until;
        this.count = count;
    }

    public RepeatInfo toEntity() {
        return RepeatInfo.builder()
                .frequency(getFrequency())
                .interval(getInterval())
                .byDay(getByDay())
                .byMonthDay(getByMonthDay())
                .until(getUntil())
                .count(getCount())
                .build();
    }
}