package com.maruhxn.todomon.domain.social.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DiligenceRankItem extends AbstractMemberInfoItem {

    // rank는 클라이언트에서 받아온 순서대로 처리
    private Long memberId;
    private int level;

    @Builder
    public DiligenceRankItem(Long memberId, String username, String profileImageUrl, int level, TitleNameItem title) {
        super(username, profileImageUrl, title);
        this.memberId = memberId;
        this.level = level;
    }
}
