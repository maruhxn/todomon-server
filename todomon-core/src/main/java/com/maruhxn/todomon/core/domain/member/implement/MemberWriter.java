package com.maruhxn.todomon.core.domain.member.implement;

import com.maruhxn.todomon.core.domain.auth.dao.RefreshTokenRepository;
import com.maruhxn.todomon.core.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.member.dto.request.UpdateMemberProfileReq;
import com.maruhxn.todomon.core.global.auth.model.Role;
import com.maruhxn.todomon.core.global.auth.model.provider.OAuth2ProviderUser;
import com.maruhxn.todomon.core.infra.file.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class MemberWriter {

    private static final String ADMIN_EMAIL = "maruhan1016@gmail.com";

    private final MemberReader memberReader;
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final FileService fileService;

    public Member registerByOAuth2(OAuth2ProviderUser oAuth2ProviderUser) {
        Role role = this.isAdmin(oAuth2ProviderUser)
                ? Role.ROLE_ADMIN
                : Role.ROLE_USER;

        Member member = oAuth2ProviderUser.toMember(role);
        memberRepository.save(member);
        return member;
    }

    private boolean isAdmin(OAuth2ProviderUser oAuth2ProviderUser) {
        return oAuth2ProviderUser.getEmail().equals(ADMIN_EMAIL);
    }

    public void modify(Member member, UpdateMemberProfileReq req) {
        if (StringUtils.hasText(req.getUsername())) member.updateUsername(req.getUsername());
        if (req.getProfileImage() != null) this.updateProfileImage(req.getProfileImage(), member);
    }

    private void updateProfileImage(MultipartFile profileImage, Member member) {
        fileService.deleteProfileImage(member.getProfileImageUrl());
        String newProfileImageUrl = fileService.storeOneFile(profileImage);
        member.updateProfileImageUrl(newProfileImageUrl);
    }

    public void withdraw(Member member) {
        fileService.deleteFile(member.getProfileImageUrl());
        memberRepository.delete(member);
        refreshTokenRepository.deleteAllByUsername(member.getUsername());
    }
}
