package com.maruhxn.todomon.domain.todo.dto.request;

import com.maruhxn.todomon.global.util.validation.ValidDateRange;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ValidDateRange
public class UpdateTodoReq {

    @Size(max = 50, message = "내용은 최대 50글자입니다.")
    private String content;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private Boolean isAllDay;
    private RepeatInfoItem repeatInfoItem;

    @Builder
    public UpdateTodoReq(String content, LocalDateTime startAt, LocalDateTime endAt, Boolean isAllDay, RepeatInfoItem repeatInfoItem) {
        this.content = content;
        this.startAt = startAt;
        this.endAt = endAt;
        this.isAllDay = isAllDay;
        this.repeatInfoItem = repeatInfoItem;
    }
}
