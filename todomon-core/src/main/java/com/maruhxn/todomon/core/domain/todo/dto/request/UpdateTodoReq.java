package com.maruhxn.todomon.core.domain.todo.dto.request;

import com.maruhxn.todomon.core.global.util.validation.ValidDateRange;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ValidDateRange
public class UpdateTodoReq implements DateRangeDto {

    @Size(max = 50, message = "내용은 최대 50글자입니다.")
    private String content;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private Boolean isAllDay;
    private String color;
    @Valid
    private RepeatInfoReqItem repeatInfoReqItem;

    @Builder
    public UpdateTodoReq(String content, LocalDateTime startAt, LocalDateTime endAt, Boolean isAllDay, String color, RepeatInfoReqItem repeatInfoReqItem) {
        this.content = content;
        this.startAt = startAt;
        this.endAt = endAt;
        this.isAllDay = isAllDay;
        this.color = color;
        this.repeatInfoReqItem = repeatInfoReqItem;
    }
}
