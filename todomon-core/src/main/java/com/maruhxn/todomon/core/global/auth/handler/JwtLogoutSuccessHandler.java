package com.maruhxn.todomon.core.global.auth.handler;

import com.maruhxn.todomon.core.global.auth.application.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.maruhxn.todomon.core.global.common.Constants.REFRESH_TOKEN_HEADER;

@Component
public class JwtLogoutSuccessHandler implements LogoutSuccessHandler {

    @Autowired
    private JwtService jwtService;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String bearerRefreshToken = request.getHeader(REFRESH_TOKEN_HEADER);
        jwtService.logout(bearerRefreshToken);
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}
