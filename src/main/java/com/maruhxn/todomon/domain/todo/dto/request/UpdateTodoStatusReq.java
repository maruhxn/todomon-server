package com.maruhxn.todomon.domain.todo.dto.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UpdateTodoStatusReq {
    private boolean isDone;
    private boolean isInstance;

    @Builder
    public UpdateTodoStatusReq(boolean isDone, boolean isInstance) {
        this.isDone = isDone;
        this.isInstance = isInstance;
    }
}
