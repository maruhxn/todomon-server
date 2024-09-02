package com.maruhxn.todomon.core.domain.social.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class CollectedPetRankItem extends AbstractMemberInfoItem {

    private Long memberId;
    private int petCnt;
    private LocalDateTime lastCollectedAt;

    @Builder
    public CollectedPetRankItem(Long memberId, String username, String profileImageUrl, int petCnt, LocalDateTime lastCollectedAt, TitleNameItem title) {
        super(username, profileImageUrl, title);
        this.memberId = memberId;
        this.petCnt = petCnt;
        this.lastCollectedAt = lastCollectedAt;
    }
}
