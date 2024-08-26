package com.maruhxn.todomon.domain.member.application;

import com.maruhxn.todomon.domain.auth.dao.RefreshTokenRepository;
import com.maruhxn.todomon.domain.member.dao.MemberQueryRepository;
import com.maruhxn.todomon.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.domain.member.domain.Member;
import com.maruhxn.todomon.domain.member.dto.request.UpdateMemberProfileReq;
import com.maruhxn.todomon.domain.member.dto.response.ProfileDto;
import com.maruhxn.todomon.domain.member.dto.response.SearchDto;
import com.maruhxn.todomon.global.auth.model.Role;
import com.maruhxn.todomon.global.auth.model.provider.OAuth2Provider;
import com.maruhxn.todomon.global.auth.model.provider.OAuth2ProviderUser;
import com.maruhxn.todomon.global.error.ErrorCode;
import com.maruhxn.todomon.global.error.exception.BadRequestException;
import com.maruhxn.todomon.global.error.exception.NotFoundException;
import com.maruhxn.todomon.infra.file.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        return memberRepository.findByEmail(oAuth2ProviderUser.getEmail())
                .orElseGet(() -> this.registerByOAuth2(oAuth2ProviderUser));
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

    @Transactional(readOnly = true)
    public ProfileDto getProfile(Long loginMemberId, Long memberId) {
        return memberQueryRepository.getMemberProfileById(loginMemberId, memberId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_MEMBER));
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
        String profileImageUrl = findMember.getProfileImageUrl();
        if (profileImageUrl.startsWith("http") || profileImageUrl.startsWith("https")) return;
        fileService.deleteFile(profileImageUrl);
    }

    public void withdraw(Long memberId) {
        Member findMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_MEMBER));
        deleteProfileImageOfFindMember(findMember);
        memberRepository.delete(findMember);
        refreshTokenRepository.deleteAllByEmail(findMember.getEmail());
    }


    public List<SearchDto> searchMemberByKey(String key) {
        return memberQueryRepository.findMemberByKey(key);
    }
}
