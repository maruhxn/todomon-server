package com.maruhxn.todomon.core.domain.member.implement;

import com.maruhxn.todomon.core.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.global.auth.model.Role;
import com.maruhxn.todomon.core.global.auth.model.provider.OAuth2ProviderUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberCreator {

    private static final String ADMIN_EMAIL = "maruhan1016@gmail.com";

    private final MemberRepository memberRepository;

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
}
