package com.maruhxn.todomon.core.domain.member.dto.request;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.maruhxn.todomon.core.domain.item.dto.request.ItemEffectReq;
import com.maruhxn.todomon.core.domain.member.domain.TitleName;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonTypeName("upsertTitleName")
public class UpsertTitleNameReq implements ItemEffectReq {

    @NotNull(message = "칭호 이름은 비어있을 수 없습니다.")
    @Size(min = 2, max = 5, message = "칭호명은 2 ~ 5 글자입니다.")
    private String name;

    @NotNull(message = "칭호 색은 비어있을 수 없습니다.")
    private String color;

    @Builder
    public UpsertTitleNameReq(String name, String color) {
        this.name = name;
        this.color = color;
    }

    public TitleName toEntity() {
        return TitleName.builder()
                .name(this.getName())
                .color(this.getColor())
                .build();
    }
}
