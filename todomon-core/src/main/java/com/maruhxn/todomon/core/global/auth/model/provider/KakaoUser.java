package com.maruhxn.todomon.core.global.auth.model.provider;

import java.util.Map;

public class KakaoUser extends OAuth2ProviderUser {

    private final Map<String, Object> kakaoAccount;
    private final Map<String, Object> kakaoProfile;

    public KakaoUser(Map<String, Object> attributes, String registrationId) {
        super(attributes, registrationId);
        this.kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        this.kakaoProfile = (Map<String, Object>) kakaoAccount.get("profile");
    }

    @Override
    public String getProviderId() {
        return "kakao_" + getAttributes().get("id");
    }

    @Override
    public String getProfileImageUrl() {
        return (String) kakaoProfile.get("profile_image_url");
    }

    @Override
    public String getEmail() {
        return (String) kakaoAccount.get("email");
    }

    @Override
    public String getUsername() {
        return (String) kakaoProfile.get("nickname");
    }
}
