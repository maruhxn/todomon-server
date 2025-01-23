package com.maruhxn.todomon.core.domain.todo.dto.request;

import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.todo.domain.Todo;
import com.maruhxn.todomon.core.global.util.validation.ValidDateRange;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ValidDateRange
public class CreateTodoReq implements DateRangeDto {

    @NotEmpty(message = "내용을 입력해주세요.")
    @Size(max = 50, message = "내용은 최대 50글자입니다.")
    private String content;
    @NotNull(message = "시작 시간을 입력해주세요.")
    private LocalDateTime startAt;
    @NotNull(message = "종료 시간을 입력해주세요.")
    private LocalDateTime endAt;
    private Boolean isAllDay;
    @NotNull(message = "색을 입력해주세요.")
    private String color;

    @Valid
    private RepeatInfoReqItem repeatInfoReqItem;

    @Builder
    public CreateTodoReq(String content, LocalDateTime startAt, LocalDateTime endAt, Boolean isAllDay, String color, RepeatInfoReqItem repeatInfoReqItem) {
        this.content = content;
        this.startAt = startAt;
        this.endAt = endAt;
        this.isAllDay = isAllDay;
        this.color = color;
        this.repeatInfoReqItem = repeatInfoReqItem;
    }

    public Todo toEntity(Member writer) {
        return Todo.builder()
                .writer(writer)
                .content(getContent())
                .startAt(getStartAt())
                .endAt(getEndAt())
                .isAllDay(getIsAllDay())
                .color(getColor())
                .build();
    }
}
