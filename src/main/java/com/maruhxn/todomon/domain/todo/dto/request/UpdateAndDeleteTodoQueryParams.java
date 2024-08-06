package com.maruhxn.todomon.domain.todo.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateAndDeleteTodoQueryParams {

    @NotNull
    private boolean isInstance;

    private TargetType targetType;

    @Builder
    public UpdateAndDeleteTodoQueryParams(boolean isInstance, TargetType targetType) {
        this.isInstance = isInstance;
        this.targetType = targetType;
    }

    public boolean getIsInstance() {
        return this.isInstance;
    }

    public void setIsInstance(boolean isInstance) {
        this.isInstance = isInstance;
    }

    public void setTargetType(TargetType targetType) {
        this.targetType = targetType;
    }
}
