package com.maruhxn.todomon.core.global.auth.model.provider;

import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.BadRequestException;

import java.util.Map;

public abstract class OAuth2ProviderUser {
    private String provider;
    private final Map<String, Object> attributes;

    protected OAuth2ProviderUser(Map<String, Object> attributes, String registrationId) {
        this.attributes = attributes;
        this.provider = registrationId;
    }

    // 팩토리 메서드
    public static OAuth2ProviderUser create(Map<String, Object> attributes, String registrationId) {
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

    public abstract String getEmail();

    public abstract String getUsername();

    public abstract String getProviderId();

    public abstract String getProfileImageUrl();

    public String getProvider() {
        return provider;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }
}