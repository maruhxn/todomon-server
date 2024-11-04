package com.maruhxn.todomon.core.global.auth.filter;

import com.maruhxn.todomon.core.global.auth.application.JwtProvider;
import com.maruhxn.todomon.core.global.auth.application.JwtService;
import com.maruhxn.todomon.core.global.auth.model.TodomonOAuth2User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtVerificationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String accessToken = jwtProvider.resolveAccessToken(request);
        if (!StringUtils.hasText(accessToken)) {
            filterChain.doFilter(request, response);
            return;
        }

        jwtProvider.validate(accessToken);
        setAuthenticationToContext(accessToken);
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return super.shouldNotFilter(request);
    }

    private void setAuthenticationToContext(String token) {
        TodomonOAuth2User todomonOAuth2User = jwtService.getPrincipal(token);
        OAuth2AuthenticationToken authentication =
                new OAuth2AuthenticationToken(
                        todomonOAuth2User,
                        todomonOAuth2User.getAuthorities(),
                        todomonOAuth2User.getProvider()
                );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
