package com.maruhxn.todomon.domain.pet.dto.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreatePetReq {

    private String name;

    @Builder
    public CreatePetReq(String name) {
        this.name = name;
    }
}
