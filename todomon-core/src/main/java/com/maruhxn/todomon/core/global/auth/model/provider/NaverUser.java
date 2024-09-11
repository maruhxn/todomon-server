package com.maruhxn.todomon.core.global.auth.model.provider;

import java.util.Map;

public class NaverUser extends OAuth2ProviderUser {

    public NaverUser(Map<String, Object> attributes, String registrationId) {
        super((Map<String, Object>) attributes.get("response"), registrationId);
    }

    @Override
    public String getEmail() {
        return (String) getAttributes().get("email");
    }

    @Override
    public String getProviderId() {
        return "naver_" + getAttributes().get("id");
    }

    @Override
    public String getProfileImageUrl() {
        return (String) getAttributes().get("profile_image");
    }

    @Override
    public String getUsername() {
        return (String) getAttributes().get("name");
    }
}
