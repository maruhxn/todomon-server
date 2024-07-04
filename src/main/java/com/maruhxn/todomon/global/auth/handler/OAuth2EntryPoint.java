package com.maruhxn.todomon.global.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maruhxn.todomon.global.common.dto.response.BaseResponse;
import com.maruhxn.todomon.global.common.dto.response.ErrorResponse;
import com.maruhxn.todomon.global.error.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2EntryPoint implements AuthenticationEntryPoint {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        BaseResponse responseDto = ErrorResponse.of(ErrorCode.UNAUTHORIZED);

        objectMapper.writeValue(response.getWriter(), responseDto);
    }
}
