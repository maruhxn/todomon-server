package com.maruhxn.todomon.core.domain.auth.dto;

import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.global.auth.model.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserInfoRes {

    private Long id;
    private String email;
    private String username;
    private boolean isSubscribed;
    private Role role;
    private String profileImage;
    private Long starPoint;
    private Long foodCnt;

    @Builder
    public UserInfoRes(Long id, String email, String username, boolean isSubscribed, Role role, String profileImage, Long starPoint, Long foodCnt) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.isSubscribed = isSubscribed;
        this.role = role;
        this.profileImage = profileImage;
        this.starPoint = starPoint;
        this.foodCnt = foodCnt;
    }

    public static UserInfoRes from(Member member) {
        return UserInfoRes.builder()
                .id(member.getId())
                .email(member.getEmail())
                .username(member.getUsername())
                .isSubscribed(member.isSubscribed())
                .role(member.getRole())
                .profileImage(member.getProfileImageUrl())
                .starPoint(member.getStarPoint())
                .foodCnt(member.getFoodCnt())
                .build();
    }
}
