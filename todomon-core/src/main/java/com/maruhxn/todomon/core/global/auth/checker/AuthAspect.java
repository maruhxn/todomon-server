package com.maruhxn.todomon.core.global.auth.checker;

import com.maruhxn.todomon.core.global.auth.model.TodomonOAuth2User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import static com.maruhxn.todomon.core.global.auth.model.Role.ROLE_ADMIN;

public class AuthAspect {

    protected boolean hasAdminAuthority() {
        return getPrincipal().getAuthorities().contains(new SimpleGrantedAuthority(ROLE_ADMIN.name()));
    }

    protected TodomonOAuth2User getPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (TodomonOAuth2User) authentication.getPrincipal();
    }
}
