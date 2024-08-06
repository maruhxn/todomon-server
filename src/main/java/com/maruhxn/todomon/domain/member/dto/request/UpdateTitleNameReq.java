package com.maruhxn.todomon.domain.member.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UpdateTitleNameReq {

    @Size(min = 2, max = 5, message = "칭호명은 2 ~ 5 글자입니다.")
    private String name;

    private String color;

    @Builder
    public UpdateTitleNameReq(String name, String color) {
        this.name = name;
        this.color = color;
    }
}
