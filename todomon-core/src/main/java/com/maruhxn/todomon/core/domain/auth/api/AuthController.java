package com.maruhxn.todomon.core.domain.auth.api;

import com.maruhxn.todomon.core.domain.member.application.MemberService;
import com.maruhxn.todomon.core.global.auth.application.JwtService;
import com.maruhxn.todomon.core.global.auth.dto.TokenDto;
import com.maruhxn.todomon.core.global.auth.dto.UserInfo;
import com.maruhxn.todomon.core.global.auth.model.TodomonOAuth2User;
import com.maruhxn.todomon.core.global.common.dto.response.BaseResponse;
import com.maruhxn.todomon.core.global.common.dto.response.DataResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.maruhxn.todomon.core.global.common.Constants.REFRESH_TOKEN_HEADER;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtService jwtService;
    private final MemberService memberService;

    @GetMapping
    public DataResponse<UserInfo> getUserInfo(
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User
    ) {
        return DataResponse.of("유저 정보 반환", memberService.getMemberInfo(todomonOAuth2User.getId()));
    }

    @GetMapping("/refresh")
    public BaseResponse refresh(
            HttpServletResponse response,
            @RequestHeader(value = REFRESH_TOKEN_HEADER) String bearerRefreshToken
    ) {
        TokenDto tokenDto = jwtService.tokenRefresh(bearerRefreshToken);
        jwtService.setCookie(tokenDto, response);
        return new BaseResponse("Token Refresh 성공");
    }

}
