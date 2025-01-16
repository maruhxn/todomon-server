package com.maruhxn.todomon.core.global.auth.dto;

import com.maruhxn.todomon.core.domain.member.domain.Member;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserInfo {
    private Long id;
    private String username;
    private boolean isSubscribed;
    private String role;
    private String profileImage;
    private Long starPoint;
    private Long foodCnt;
    private String provider;

    @Builder
    public UserInfo(Long id, String username, boolean isSubscribed, String role, String profileImage, Long starPoint, Long foodCnt, String provider) {
        this.id = id;
        this.username = username;
        this.isSubscribed = isSubscribed;
        this.role = role;
        this.profileImage = profileImage;
        this.starPoint = starPoint;
        this.foodCnt = foodCnt;
        this.provider = provider;
    }

    public static UserInfo from(Member member) {
        return UserInfo.builder()
                .id(member.getId())
                .username(member.getUsername())
                .isSubscribed(member.isSubscribed())
                .role(member.getRole().name())
                .profileImage(member.getProfileImageUrl())
                .starPoint(member.getStarPoint())
                .foodCnt(member.getFoodCnt())
                .provider(member.getProvider().name())
                .build();
    }
}
