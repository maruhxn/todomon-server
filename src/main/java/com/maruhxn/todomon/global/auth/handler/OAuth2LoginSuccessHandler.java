package com.maruhxn.todomon.global.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maruhxn.todomon.global.auth.application.JwtProvider;
import com.maruhxn.todomon.global.auth.application.JwtService;
import com.maruhxn.todomon.global.auth.dto.TokenDto;
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

//    @Value("${client.url}")
//    private String clientUrl;

    @Autowired
    private JwtProvider jwtProvider;
    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        TodomonOAuth2User principal = (TodomonOAuth2User) authentication.getPrincipal();
        TokenDto tokenDto = jwtProvider.createJwt(principal);
//        String targetUri = createUri(tokenDto, principal.getName());
        jwtService.saveRefreshToken(principal, tokenDto);
        jwtProvider.setHeader(response, tokenDto);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        DataResponse<TokenDto> dto = DataResponse.of("로그인 성공", tokenDto);
        objectMapper.writeValue(response.getWriter(), dto);
    }

//    private String createUri(TokenDto tokenDto, String username) {
//        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
//        queryParams.add("username", username);
//        queryParams.add("accessToken", tokenDto.getAccessToken());
//        queryParams.add("refreshToken", tokenDto.getRefreshToken());
//
//        return UriComponentsBuilder
//                .fromHttpUrl(clientUrl + "/auth")
//                .queryParams(queryParams)
//                .build()
//                .encode(StandardCharsets.UTF_8)
//                .toUriString();
//    }
}
