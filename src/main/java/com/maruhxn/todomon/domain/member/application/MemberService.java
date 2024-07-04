package com.maruhxn.todomon.domain.member.application;

import com.maruhxn.todomon.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.domain.member.domain.Member;
import com.maruhxn.todomon.global.auth.model.Role;
import com.maruhxn.todomon.global.auth.model.provider.OAuth2Provider;
import com.maruhxn.todomon.global.auth.model.provider.OAuth2ProviderUser;
import com.maruhxn.todomon.global.error.ErrorCode;
import com.maruhxn.todomon.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public Member createOrUpdate(OAuth2ProviderUser oAuth2ProviderUser) {
        Member member = null;
        Optional<Member> optionalMember = memberRepository.findByEmail(oAuth2ProviderUser.getEmail());

        if (optionalMember.isPresent()) {
            member = optionalMember.get();
            member.updateByOAuth2Info(oAuth2ProviderUser);
        } else {
            member = this.registerByOAuth2(oAuth2ProviderUser);
        }

        if (member == null) throw new NotFoundException(ErrorCode.NOT_FOUND_MEMBER);

        return member;
    }

    public Member registerByOAuth2(OAuth2ProviderUser oAuth2ProviderUser) {
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

        memberRepository.save(member);
        return member;
    }
}
