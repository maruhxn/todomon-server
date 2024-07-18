package com.maruhxn.todomon.domain.pet.dto.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreatePetReq {

    private String name;
    private String color;

    @Builder
    public CreatePetReq(String name, String color) {
        this.name = name;
        this.color = color;
    }
}
