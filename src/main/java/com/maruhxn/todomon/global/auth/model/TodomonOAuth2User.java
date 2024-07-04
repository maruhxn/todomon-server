package com.maruhxn.todomon.global.auth.model;

import com.maruhxn.todomon.domain.member.domain.Member;
import com.maruhxn.todomon.global.auth.model.provider.OAuth2ProviderUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class TodomonOAuth2User implements OAuth2User {

    private Map<String, Object> attributes;
    private Member member;

    private TodomonOAuth2User(Member member) {
        this.member = member;
    }

    private TodomonOAuth2User(Member member, OAuth2ProviderUser oAuth2ProviderUser) {
        this.member = member;
        this.attributes = oAuth2ProviderUser.getAttributes();
    }

    public static OAuth2User of(Member member, OAuth2ProviderUser oAuth2ProviderUser) {
        return new TodomonOAuth2User(member, oAuth2ProviderUser);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority(member.getRole().name());
        authorities.add(simpleGrantedAuthority);
        return authorities;
    }

    @Override
    public String getName() {
        return this.member.getUsername();
    }
}
