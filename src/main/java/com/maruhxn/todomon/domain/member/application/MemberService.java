package com.maruhxn.todomon.domain.member.application;

import com.maruhxn.todomon.domain.auth.dao.RefreshTokenRepository;
import com.maruhxn.todomon.domain.member.dao.MemberQueryRepository;
import com.maruhxn.todomon.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.domain.member.dao.TitleNameRepository;
import com.maruhxn.todomon.domain.member.domain.Member;
import com.maruhxn.todomon.domain.member.dto.request.UpdateMemberProfileReq;
import com.maruhxn.todomon.domain.member.dto.response.ProfileDto;
import com.maruhxn.todomon.global.auth.model.Role;
import com.maruhxn.todomon.global.auth.model.provider.OAuth2Provider;
import com.maruhxn.todomon.global.auth.model.provider.OAuth2ProviderUser;
import com.maruhxn.todomon.global.error.ErrorCode;
import com.maruhxn.todomon.global.error.exception.BadRequestException;
import com.maruhxn.todomon.global.error.exception.ExistingResourceException;
import com.maruhxn.todomon.global.error.exception.NotFoundException;
import com.maruhxn.todomon.infra.file.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberQueryRepository memberQueryRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private final FileService fileService;

    public Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_MEMBER));
    }

    public Member createOrUpdate(OAuth2ProviderUser oAuth2ProviderUser) {
        memberRepository.findByEmail(oAuth2ProviderUser.getEmail())
                .ifPresent(findMember -> {
                    throw new ExistingResourceException(ErrorCode.EXISTING_MEMBER);
                });

//        if (optionalMember.isPresent()) {
//            member = optionalMember.get();
//            member.updateByOAuth2Info(oAuth2ProviderUser);
//        } else {
//            member = this.registerByOAuth2(oAuth2ProviderUser);
//        }

        return this.registerByOAuth2(oAuth2ProviderUser);
    }

    private Member registerByOAuth2(OAuth2ProviderUser oAuth2ProviderUser) {
        Role role = oAuth2ProviderUser.getEmail().equals("maruhan1016@gmail.com")
                ? Role.ROLE_ADMIN
                : Role.ROLE_USER;

        Member member = Member.builder()
                .username(oAuth2ProviderUser.getUsername())
                .email(oAuth2ProviderUser.getEmail())
                .provider(OAuth2Provider.valueOf(oAuth2ProviderUser.getProvider().toUpperCase()))
                .providerId(oAuth2ProviderUser.getProviderId())
                .profileImageUrl(oAuth2ProviderUser.getProfileImageUrl())
                .role(role)
                .build();
        member.initDiligence();

        memberRepository.save(member);
        return member;
    }

    // 유저명, 프로필사진, 이메일, 대표 펫, 팔로워 수, 팔로잉 수, 칭호
    @Transactional(readOnly = true)
    public ProfileDto getProfile(Long memberId) {
        return memberQueryRepository.getMemberProfileById(memberId);
    }

    public void updateProfile(Long memberId, UpdateMemberProfileReq req) {
        validateUpdateProfileRequest(req);
        Member findMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_MEMBER));


        String newProfileImageUrl = null;
        if (req.getProfileImage() != null) {
            newProfileImageUrl = fileService.storeOneFile(req.getProfileImage());
            deleteProfileImageOfFindMember(findMember);
        }

        findMember.updateProfile(
                req.getUsername(),
                newProfileImageUrl
        );
    }

    private void validateUpdateProfileRequest(UpdateMemberProfileReq updateMemberProfileReq) {
        if (
                updateMemberProfileReq.getUsername() == null
                        && updateMemberProfileReq.getProfileImage() == null) {
            throw new BadRequestException(ErrorCode.VALIDATION_ERROR, "수정할 데이터를 전달해야 합니다.");
        }
    }

    private void deleteProfileImageOfFindMember(Member findMember) {
        if (findMember.getProfileImageUrl().startsWith("http")) return;
        fileService.deleteFile(findMember.getProfileImageUrl());
    }

    public void withdraw(Long memberId) {
        Member findMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_MEMBER));
        deleteProfileImageOfFindMember(findMember);
        memberRepository.delete(findMember);
        refreshTokenRepository.deleteAllByEmail(findMember.getEmail());
    }


}
