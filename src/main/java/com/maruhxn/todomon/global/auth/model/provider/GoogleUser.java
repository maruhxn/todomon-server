package com.maruhxn.todomon.global.auth.model.provider;

import java.util.Map;

public class GoogleUser extends OAuth2ProviderUser {

    public GoogleUser(Map<String, Object> attributes, String registrationId) {
        super(attributes, registrationId);
    }

    @Override
    public String getProviderId() {
        return "google_" + getAttributes().get("sub");
    }

    @Override
    public String getProfileImageUrl() {
        return (String) getAttributes().get("picture");
    }

    @Override
    public String getEmail() {
        return (String) getAttributes().get("email");
    }

    @Override
    public String getUsername() {
        return (String) getAttributes().get("name");
    }
}