package com.maruhxn.todomon.domain.member.dto.response;

import com.maruhxn.todomon.domain.member.domain.TitleName;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TitleNameItem {

    private Long titleNameId;
    private String name;
    private String color;

    @Builder
    public TitleNameItem(Long titleNameId, String name, String color) {
        this.titleNameId = titleNameId;
        this.name = name;
        this.color = color;
    }

    public static TitleNameItem of(TitleName titleName) {
        return TitleNameItem.builder()
                .titleNameId(titleName.getId())
                .name(titleName.getName())
                .color(titleName.getColor())
                .build();
    }
}
