package com.maruhxn.todomon.domain.social.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TodoAchievementRankItem extends AbstractMemberInfoItem {

    private Long memberId;
    private long cnt;

    @Builder
    public TodoAchievementRankItem(Long memberId, String username, String profileImageUrl, long cnt, TitleNameItem title) {
        super(username, profileImageUrl, title);
        this.memberId = memberId;
        this.cnt = cnt;
    }
}
