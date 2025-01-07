package com.maruhxn.todomon.core.domain.member.application;

import com.maruhxn.todomon.core.domain.auth.dao.RefreshTokenRepository;
import com.maruhxn.todomon.core.domain.auth.dto.UserInfoRes;
import com.maruhxn.todomon.core.domain.member.dao.MemberQueryRepository;
import com.maruhxn.todomon.core.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.member.dto.request.UpdateMemberProfileReq;
import com.maruhxn.todomon.core.domain.member.dto.response.MemberSearchRes;
import com.maruhxn.todomon.core.domain.member.dto.response.ProfileRes;
import com.maruhxn.todomon.core.global.auth.checker.IsMeOrAdmin;
import com.maruhxn.todomon.core.global.auth.model.Role;
import com.maruhxn.todomon.core.global.auth.model.provider.OAuth2ProviderUser;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.BadRequestException;
import com.maruhxn.todomon.core.global.error.exception.NotFoundException;
import com.maruhxn.todomon.core.infra.file.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private static final String ADMIN_EMAIL = "maruhan1016@gmail.com";
    private final MemberRepository memberRepository;
    private final MemberQueryRepository memberQueryRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private final FileService fileService;

    @Transactional(readOnly = true)
    public List<MemberSearchRes> searchMemberByKey(String key) {
        return memberQueryRepository.findMemberByNameKey(key);
    }

    @Transactional(readOnly = true)
    public UserInfoRes getMemberInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_MEMBER));
        return UserInfoRes.from(member);
    }

    @Transactional(readOnly = true)
    public Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_MEMBER));
    }

    public Member getOrCreate(OAuth2ProviderUser oAuth2ProviderUser) {
        return memberRepository.findByEmail(oAuth2ProviderUser.getEmail())
                .orElseGet(() -> this.registerByOAuth2(oAuth2ProviderUser));
    }

    private Member registerByOAuth2(OAuth2ProviderUser oAuth2ProviderUser) {
        Role role = isAdmin(oAuth2ProviderUser)
                ? Role.ROLE_ADMIN
                : Role.ROLE_USER;

        Member member = Member.of(oAuth2ProviderUser, role);

        memberRepository.save(member);
        return member;
    }

    private boolean isAdmin(OAuth2ProviderUser oAuth2ProviderUser) {
        return oAuth2ProviderUser.getEmail().equals(ADMIN_EMAIL);
    }

    @Transactional(readOnly = true)
    public ProfileRes getProfile(Long loginMemberId, Long targetMemberId) {
        return memberQueryRepository.getMemberProfileById(loginMemberId, targetMemberId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_MEMBER));
    }

    @IsMeOrAdmin
    public void updateProfile(Long memberId, UpdateMemberProfileReq req) {
        this.validateUpdateProfileRequest(req);
        Member findMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_MEMBER));

        if (StringUtils.hasText(req.getUsername())) findMember.updateUsername(req.getUsername());

        if (req.getProfileImage() != null) this.updateProfileImage(req.getProfileImage(), findMember);

    }

    private void validateUpdateProfileRequest(UpdateMemberProfileReq updateMemberProfileReq) {
        if (updateMemberProfileReq.getUsername() == null
                && updateMemberProfileReq.getProfileImage() == null)
            throw new BadRequestException(ErrorCode.VALIDATION_ERROR, "수정할 데이터를 전달해야 합니다.");
    }

    private void updateProfileImage(MultipartFile profileImage, Member findMember) {
        this.deleteProfileImageOfFindMember(findMember.getProfileImageUrl());
        String newProfileImageUrl = fileService.storeOneFile(profileImage);
        findMember.updateProfileImageUrl(newProfileImageUrl);
    }

    private void deleteProfileImageOfFindMember(String profileImageUrl) {
        if (profileImageUrl.startsWith("http") || profileImageUrl.startsWith("https")) return;
        fileService.deleteFile(profileImageUrl);
    }

    @IsMeOrAdmin
    public void withdraw(Long memberId) {
        Member findMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_MEMBER));

        this.deleteProfileImageOfFindMember(findMember.getProfileImageUrl());

        memberRepository.delete(findMember);

        refreshTokenRepository.deleteAllByEmail(findMember.getEmail());
    }
}
