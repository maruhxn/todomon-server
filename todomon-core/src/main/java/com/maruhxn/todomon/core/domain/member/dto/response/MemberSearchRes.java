package com.maruhxn.todomon.core.domain.member.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberSearchRes {

    private Long memberId;
    private String username;

    @Builder
    public MemberSearchRes(Long memberId, String username) {
        this.memberId = memberId;
        this.username = username;
    }
}
