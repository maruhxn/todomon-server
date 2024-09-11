package com.maruhxn.todomon.core.domain.social.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FollowerItem extends AbstractMemberInfoItem {

    private Long followerId;

    public FollowerItem(Long followerId, String username, String profileImageUrl, TitleNameItem title) {
        super(username, profileImageUrl, title);
        this.followerId = followerId;
    }

}
