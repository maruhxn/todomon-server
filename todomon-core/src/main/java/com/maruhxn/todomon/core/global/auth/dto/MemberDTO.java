package com.maruhxn.todomon.core.global.auth.dto;

import com.maruhxn.todomon.core.domain.member.domain.Member;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberDTO {
    private Long id;
    private String username;
    private String email;
    private String role;
    private String provider;

    @Builder
    public MemberDTO(Long id, String username, String email, String role, String provider) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.provider = provider;
    }

    public static MemberDTO from(Member member) {
        return MemberDTO.builder()
                .id(member.getId())
                .username(member.getUsername())
                .email(member.getEmail())
                .role(member.getRole().name())
                .provider(member.getProvider().name())
                .build();
    }
}
