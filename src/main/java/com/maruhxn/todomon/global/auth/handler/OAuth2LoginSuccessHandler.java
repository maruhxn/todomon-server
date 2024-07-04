package com.maruhxn.todomon.global.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maruhxn.todomon.global.auth.model.TodomonOAuth2User;
import com.maruhxn.todomon.global.common.dto.response.DataResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        TodomonOAuth2User principal = (TodomonOAuth2User) authentication.getPrincipal();
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        DataResponse<TodomonOAuth2User> dto = DataResponse.of("로그인 성공", principal);
        System.out.println(dto);
        objectMapper.writeValue(response.getWriter(), dto);
    }
}
