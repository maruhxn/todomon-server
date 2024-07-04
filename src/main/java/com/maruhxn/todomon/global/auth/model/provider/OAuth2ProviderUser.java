package com.maruhxn.todomon.global.auth.model.provider;

import java.util.Map;

public abstract class OAuth2ProviderUser {
    private String provider;
    private final Map<String, Object> attributes;

    public OAuth2ProviderUser(Map<String, Object> attributes, String registrationId) {
        this.attributes = attributes;
        this.provider = registrationId;
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