package com.maruhxn.todomon.core.global.auth.model;

import com.maruhxn.todomon.core.global.auth.dto.MemberDTO;
import com.maruhxn.todomon.core.global.auth.model.provider.OAuth2ProviderUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class TodomonOAuth2User implements OAuth2User {

    private Map<String, Object> attributes;
    private MemberDTO memberDTO;

    public TodomonOAuth2User(MemberDTO memberDTO) {
        this.memberDTO = memberDTO;
    }

    private TodomonOAuth2User(MemberDTO memberDTO, OAuth2ProviderUser oAuth2ProviderUser) {
        this.memberDTO = memberDTO;
        this.attributes = oAuth2ProviderUser.getAttributes();
    }

    public static TodomonOAuth2User from(MemberDTO dto) {
        return new TodomonOAuth2User(dto);
    }

    public static TodomonOAuth2User of(MemberDTO dto, OAuth2ProviderUser oAuth2ProviderUser) {
        return new TodomonOAuth2User(dto, oAuth2ProviderUser);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority(memberDTO.getRole());
        authorities.add(simpleGrantedAuthority);
        return authorities;
    }

    @Override
    public String getName() {
        return this.memberDTO.getUsername();
    }

    public Long getId() {
        return this.memberDTO.getId();
    }

    public String getEmail() {
        return this.memberDTO.getEmail();
    }

    public String getRole() {
        return this.memberDTO.getRole();
    }

    public String getProvider() {
        return this.memberDTO.getProvider();
    }
}
