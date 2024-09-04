package com.maruhxn.todomon.batch.vo;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class MemberAchievementDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long memberId;
    private Long achievementCount;

    @Builder
    public MemberAchievementDTO(Long memberId, Long achievementCount) {
        this.memberId = memberId;
        this.achievementCount = achievementCount;
    }
}
