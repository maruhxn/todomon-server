package com.maruhxn.todomon.core.domain.social.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FollowingItem extends AbstractMemberInfoItem {

    private Long followeeId;

    @Builder
    public FollowingItem(Long followeeId, String username, String profileImageUrl, TitleNameItem title) {
        super(username, profileImageUrl, title);
        this.followeeId = followeeId;
    }
}
