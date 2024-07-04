package com.maruhxn.todomon.global.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maruhxn.todomon.global.common.dto.response.BaseResponse;
import com.maruhxn.todomon.global.common.dto.response.ErrorResponse;
import com.maruhxn.todomon.global.error.ErrorCode;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JwtVerification Filter에서 발생하는 에러를 핸들링하기 위한 보조 필터
 */
@Component
@RequiredArgsConstructor
public class JwtExceptionFilter extends OncePerRequestFilter {


    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response); // JwtAuthenticationFilter로 이동
        } catch (JwtException ex) {
            // JwtAuthenticationFilter에서 예외 발생하면 바로 setErrorResponse 호출
            setErrorResponse(request, response, ex);
        }
    }

    public void setErrorResponse(HttpServletRequest request, HttpServletResponse response, Throwable ex) throws IOException {

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        BaseResponse responseDto = ErrorResponse.of(ErrorCode.INVALID_TOKEN, ex.getMessage());

        objectMapper.writeValue(response.getWriter(), responseDto);
    }

}
