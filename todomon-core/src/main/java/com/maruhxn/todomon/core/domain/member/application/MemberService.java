package com.maruhxn.todomon.core.domain.member.application;

import com.maruhxn.todomon.core.domain.auth.dto.UserInfoRes;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.member.dto.request.UpdateMemberProfileReq;
import com.maruhxn.todomon.core.domain.member.dto.response.MemberSearchRes;
import com.maruhxn.todomon.core.domain.member.dto.response.ProfileRes;
import com.maruhxn.todomon.core.domain.member.implement.MemberCreator;
import com.maruhxn.todomon.core.domain.member.implement.MemberModifier;
import com.maruhxn.todomon.core.domain.member.implement.MemberReader;
import com.maruhxn.todomon.core.domain.member.implement.MemberRemover;
import com.maruhxn.todomon.core.global.auth.checker.IsMeOrAdmin;
import com.maruhxn.todomon.core.global.auth.model.provider.OAuth2ProviderUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberReader memberReader;
    private final MemberCreator memberCreator;
    private final MemberModifier memberModifier;
    private final MemberRemover memberRemover;


    @Transactional(readOnly = true)
    public List<MemberSearchRes> searchMemberByKey(String key) {
        return memberReader.search(key);
    }

    @Transactional(readOnly = true)
    public UserInfoRes getMemberInfo(Long memberId) {
        return UserInfoRes.from(memberReader.findById(memberId));
    }

    @Transactional(readOnly = true)
    public ProfileRes getProfile(Long loginMemberId, Long targetMemberId) {
        return memberReader.getProfile(loginMemberId, targetMemberId);
    }

    @Transactional
    public Member getOrCreate(OAuth2ProviderUser oAuth2ProviderUser) {
        String email = oAuth2ProviderUser.getEmail();
        return memberReader.findOptionalByEmail(email)
                .orElseGet(() -> memberCreator.registerByOAuth2(oAuth2ProviderUser));
    }

    @Transactional
    @IsMeOrAdmin
    public void updateProfile(Long memberId, UpdateMemberProfileReq req) {
        Member member = memberReader.findById(memberId);
        memberModifier.modify(member, req);
    }

    @Transactional
    @IsMeOrAdmin
    public void withdraw(Long memberId) {
        Member member = memberReader.findById(memberId);
        memberRemover.withdraw(member);
    }
}
