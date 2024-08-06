package com.maruhxn.todomon.domain.member.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreateTitleNameReq {

    @NotNull(message = "칭호 이름은 비어있을 수 없습니다.")
    @Size(min = 2, max = 5, message = "칭호명은 2 ~ 5 글자입니다.")
    private String name;

    @NotNull(message = "칭호 색은 비어있을 수 없습니다.")
    private String color;

    @Builder
    public CreateTitleNameReq(String name, String color) {
        this.name = name;
        this.color = color;
    }
}
