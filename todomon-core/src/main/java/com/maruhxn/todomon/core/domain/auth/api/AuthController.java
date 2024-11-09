package com.maruhxn.todomon.core.domain.auth.api;

import com.maruhxn.todomon.core.domain.auth.dto.UserInfoDto;
import com.maruhxn.todomon.core.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.global.auth.application.JwtService;
import com.maruhxn.todomon.core.global.auth.dto.TokenDto;
import com.maruhxn.todomon.core.global.auth.model.TodomonOAuth2User;
import com.maruhxn.todomon.core.global.common.dto.response.DataResponse;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.NotFoundException;
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
    private final MemberRepository memberRepository;

    @GetMapping
    public DataResponse<UserInfoDto> getUserInfo(
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User
    ) {
        Member member = memberRepository.findById(todomonOAuth2User.getId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_MEMBER));
        UserInfoDto userInfo = UserInfoDto.from(member);
        return DataResponse.of("유저 정보 반환", userInfo);
    }

    @GetMapping("/refresh")
    public DataResponse<TokenDto> refresh(
            HttpServletResponse response,
            @RequestHeader(value = REFRESH_TOKEN_HEADER) String bearerRefreshToken
    ) {
        TokenDto tokenDto = jwtService.refresh(bearerRefreshToken, response);
        return DataResponse.of("Token Refresh 성공", tokenDto);
    }

}
