package com.maruhxn.todomon.core.global.auth.model;

import com.maruhxn.todomon.core.global.auth.dto.UserInfo;
import com.maruhxn.todomon.core.global.auth.model.provider.OAuth2ProviderUser;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@ToString
public class TodomonOAuth2User implements OAuth2User {

    private Map<String, Object> attributes;
    private UserInfo userInfo;

    public TodomonOAuth2User(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    private TodomonOAuth2User(UserInfo userInfo, OAuth2ProviderUser oAuth2ProviderUser) {
        this.userInfo = userInfo;
        this.attributes = oAuth2ProviderUser.getAttributes();
    }

    public static TodomonOAuth2User from(UserInfo dto) {
        return new TodomonOAuth2User(dto);
    }

    public static TodomonOAuth2User of(UserInfo dto, OAuth2ProviderUser oAuth2ProviderUser) {
        return new TodomonOAuth2User(dto, oAuth2ProviderUser);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority(userInfo.getRole());
        authorities.add(simpleGrantedAuthority);
        return authorities;
    }

    @Override
    public String getName() {
        return this.userInfo.getUsername();
    }

    public Long getId() {
        return this.userInfo.getId();
    }

    public String getRole() {
        return this.userInfo.getRole();
    }

    public String getProfileImage() {
        return this.userInfo.getProfileImage();
    }

    public boolean getIsSubscribed() {
        return this.userInfo.isSubscribed();
    }

    public Long getStarPoint() {
        return this.userInfo.getStarPoint();
    }

    public Long getFoodCnt() {
        return this.userInfo.getFoodCnt();
    }

    public String getProvider() {
        return this.userInfo.getProvider();
    }
}
