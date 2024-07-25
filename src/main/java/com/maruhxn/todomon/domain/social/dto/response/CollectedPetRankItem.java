package com.maruhxn.todomon.domain.social.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class CollectedPetRankItem {

    private Long id;
    private String username;
    private String profileImageUrl;
    private int petCnt;
    private LocalDateTime lastCollectedAt;

    @Builder
    public CollectedPetRankItem(Long id, String username, String profileImageUrl, int petCnt, LocalDateTime lastCollectedAt) {
        this.id = id;
        this.username = username;
        this.profileImageUrl = profileImageUrl;
        this.petCnt = petCnt;
        this.lastCollectedAt = lastCollectedAt;
    }
}
