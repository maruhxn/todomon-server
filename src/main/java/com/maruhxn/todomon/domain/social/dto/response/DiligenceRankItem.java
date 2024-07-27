package com.maruhxn.todomon.domain.social.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DiligenceRankItem {

    // rank는 클라이언트에서 받아온 순서대로 처리
    private Long id;
    private String username;
    private String profileImageUrl;
    private int level;

    @Builder
    public DiligenceRankItem(Long id, String username, String profileImageUrl, int level) {
        this.id = id;
        this.username = username;
        this.profileImageUrl = profileImageUrl;
        this.level = level;
    }
}
