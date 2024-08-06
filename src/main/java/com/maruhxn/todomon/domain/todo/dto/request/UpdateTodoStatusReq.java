package com.maruhxn.todomon.domain.todo.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UpdateTodoStatusReq {
    @NotNull(message = "완료 여부는 비어있을 수 없습니다.")
    private Boolean isDone;

    @Builder
    public UpdateTodoStatusReq(Boolean isDone) {
        this.isDone = isDone;
    }
}
