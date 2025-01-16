package com.maruhxn.todomon.core.global.auth.handler;

import com.maruhxn.todomon.core.global.auth.application.JwtService;
import com.maruhxn.todomon.core.global.auth.dto.TokenDto;
import com.maruhxn.todomon.core.global.auth.model.TodomonOAuth2User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Value("${client.url}")
    private String clientUrl;

    @Autowired
    private JwtService jwtService;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        TodomonOAuth2User principal = (TodomonOAuth2User) authentication.getPrincipal();
        TokenDto tokenDto = jwtService.doTokenGenerationProcess(principal);
        jwtService.setCookie(tokenDto, response);
        String targetUri = this.createUri(tokenDto, principal.getId());
        response.sendRedirect(targetUri);
    }

    private String createUri(TokenDto tokenDto, Long memberId) {
//        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
//        queryParams.add("memberId", memberId.toString());
//        queryParams.add("accessToken", tokenDto.getAccessToken());
//        queryParams.add("refreshToken", tokenDto.getRefreshToken());

        return UriComponentsBuilder
                .fromHttpUrl(clientUrl)
//                .queryParams(queryParams)
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUriString();
    }
}
