package com.maruhxn.todomon.domain.auth.dto;

import com.maruhxn.todomon.domain.member.domain.Member;
import com.maruhxn.todomon.global.auth.model.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserInfoDto {

    private Long id;
    private String email;
    private String username;
    private Role role;
    private String profileImage;

    @Builder
    public UserInfoDto(Long id, String email, String username, Role role, String profileImage) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.role = role;
        this.profileImage = profileImage;
    }

    public static UserInfoDto from(Member member) {
        return UserInfoDto.builder()
                .id(member.getId())
                .email(member.getEmail())
                .username(member.getUsername())
                .role(member.getRole())
                .profileImage(member.getProfileImageUrl())
                .build();
    }
}
