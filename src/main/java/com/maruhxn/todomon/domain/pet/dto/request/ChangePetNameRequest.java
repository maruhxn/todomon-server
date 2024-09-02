package com.maruhxn.todomon.domain.pet.dto.request;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.maruhxn.todomon.domain.item.dto.request.ItemEffectRequest;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonTypeName("changePetName")
public class ChangePetNameRequest implements ItemEffectRequest {

    @NotNull(message = "펫 아이디는 비어있을 수 없습니다.")
    private Long petId;

    @NotEmpty(message = "이름은 비어있을 수 없습니다.")
    private String name;

    private String color;

    @Builder
    public ChangePetNameRequest(Long petId, String name, String color) {
        this.petId = petId;
        this.name = name;
        this.color = color;
    }
}
