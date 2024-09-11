package com.maruhxn.todomon.core.domain.member.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SearchDto {

    private Long memberId;
    private String username;

    @Builder
    public SearchDto(Long memberId, String username) {
        this.memberId = memberId;
        this.username = username;
    }
}
