package com.maruhxn.todomon.domain.social.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MyFollowerItem extends FollowerItem {

    private boolean isMatFollow;

    public MyFollowerItem(Long followerId, String username, String profileImageUrl, TitleNameItem titleNameItem, boolean isMatFollow) {
        super(followerId, username, profileImageUrl, titleNameItem);
        this.isMatFollow = isMatFollow;
    }

}
