package com.maruhxn.todomon.core.global.auth.application;

import com.maruhxn.todomon.core.domain.member.application.MemberService;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.global.auth.model.TodomonOAuth2User;
import com.maruhxn.todomon.core.global.auth.model.provider.KakaoUser;
import com.maruhxn.todomon.core.global.auth.model.provider.OAuth2ProviderUser;
import com.maruhxn.todomon.core.global.auth.model.provider.GoogleUser;
import com.maruhxn.todomon.core.global.auth.model.provider.NaverUser;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class TodomonOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final MemberService memberService;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        ClientRegistration clientRegistration = userRequest.getClientRegistration();
        OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = oAuth2UserService.loadUser(userRequest); // 인가 서버와 통신해서 실제 사용자 정보 조회
        OAuth2ProviderUser oAuth2ProviderUser = this.getOAuth2ProviderUser(clientRegistration, oAuth2User);

        // 회원가입
        Member member = memberService.createOrUpdate(oAuth2ProviderUser);
        return TodomonOAuth2User.of(member, oAuth2ProviderUser);
    }

    private OAuth2ProviderUser getOAuth2ProviderUser(ClientRegistration clientRegistration, OAuth2User oAuth2User) {
        String registrationId = clientRegistration.getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();
        switch (registrationId) {
            case "google":
                return new GoogleUser(attributes, registrationId);
            case "naver":
                return new NaverUser(attributes, registrationId);
            case "kakao":
                return new KakaoUser(attributes, registrationId);
            default:
                throw new BadRequestException(ErrorCode.BAD_REQUEST, "일치하는 제공자가 없습니다.");
        }
    }
}
