package com.maruhxn.todomon.domain.social.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TodoAchievementRankItem {

    private Long id;
    private String username;
    private String profileImageUrl;
    private long cnt;

    @Builder
    public TodoAchievementRankItem(Long id, String username, String profileImageUrl, long cnt) {
        this.id = id;
        this.username = username;
        this.profileImageUrl = profileImageUrl;
        this.cnt = cnt;
    }
}
