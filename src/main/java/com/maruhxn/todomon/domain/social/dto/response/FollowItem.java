package com.maruhxn.todomon.domain.social.dto.response;

import com.maruhxn.todomon.domain.member.domain.Member;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FollowItem {

    private Long id;
    private String username;
    private String profileImageUrl;

    @Builder
    public FollowItem(Long id, String username, String profileImageUrl) {
        this.id = id;
        this.username = username;
        this.profileImageUrl = profileImageUrl;
    }

    public static FollowItem of(Member member) {
        return FollowItem.builder()
                .id(member.getId())
                .username(member.getUsername())
                .profileImageUrl(member.getProfileImageUrl())
                .build();
    }
}
