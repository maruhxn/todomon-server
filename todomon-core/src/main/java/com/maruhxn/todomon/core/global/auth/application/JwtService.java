package com.maruhxn.todomon.core.global.auth.application;

import com.maruhxn.todomon.core.domain.auth.domain.RefreshToken;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.member.implement.MemberReader;
import com.maruhxn.todomon.core.global.auth.dto.TokenDto;
import com.maruhxn.todomon.core.global.auth.dto.UserInfo;
import com.maruhxn.todomon.core.global.auth.implement.JwtProvider;
import com.maruhxn.todomon.core.global.auth.implement.RefreshTokenReader;
import com.maruhxn.todomon.core.global.auth.implement.RefreshTokenWriter;
import com.maruhxn.todomon.core.global.auth.model.TodomonOAuth2User;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.UnauthorizedException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import static com.maruhxn.todomon.core.global.auth.implement.JwtProvider.BEARER_PREFIX;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProvider jwtProvider;
    private final MemberReader memberReader;
    private final RefreshTokenReader refreshTokenReader;
    private final RefreshTokenWriter refreshTokenWriter;

    public TodomonOAuth2User getPrincipal(String accessToken) {
        return TodomonOAuth2User.from(jwtProvider.extractMemberDTOFromAccessToken(accessToken));
    }

    @Transactional
    public void logout(String bearerRefreshToken) {
        String username = this.extractUsernameFromRefreshToken(bearerRefreshToken);
        log.debug("로그아웃 === 유저명: {}", username);
        refreshTokenWriter.removeAllByUsername(username);
    }

    private String extractUsernameFromRefreshToken(String bearerRefreshToken) {
        String refreshToken = this.getTokenFromBearer(bearerRefreshToken);
        return jwtProvider.getUsername(refreshToken);
    }

    public void validate(String token) {
        try {
            jwtProvider.getPayload(token);
        } catch (SecurityException e) {
            throw new UnauthorizedException(ErrorCode.INVALID_TOKEN, "검증 정보가 올바르지 않습니다.");
        } catch (MalformedJwtException e) {
            throw new UnauthorizedException(ErrorCode.INVALID_TOKEN, "유효하지 않은 토큰입니다.");
        } catch (ExpiredJwtException e) {
            throw new UnauthorizedException(ErrorCode.INVALID_TOKEN, "기한이 만료된 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            throw new UnauthorizedException(ErrorCode.INVALID_TOKEN, "지원되지 않는 토큰입니다.");
        }
    }

    @Transactional
    public TokenDto tokenRefresh(String bearerRefreshToken) {
        String refreshToken = this.getTokenFromBearer(bearerRefreshToken);
        this.validate(refreshToken);
        TodomonOAuth2User todomonOAuth2User = this.extractTodomonOAuth2User(refreshToken);
        log.debug("토큰 갱신 === 유저: {}", todomonOAuth2User);
        return this.doTokenGenerationProcess(todomonOAuth2User);
    }

    private TodomonOAuth2User extractTodomonOAuth2User(String refreshToken) {
        RefreshToken findRefreshToken = refreshTokenReader.findByPayload(refreshToken);
        Member member = memberReader.findByUsername(findRefreshToken.getUsername());
        return TodomonOAuth2User.from(UserInfo.from(member));
    }

    @Transactional
    public TokenDto doTokenGenerationProcess(TodomonOAuth2User principal) {
        TokenDto tokenDto = jwtProvider.createJwt(principal);
        refreshTokenWriter.upsertRefreshToken(tokenDto);
        return tokenDto;
    }

    public void setCookie(TokenDto tokenDto, HttpServletResponse response) {
        Cookie accessTokenCookie = new Cookie("accessToken", tokenDto.getAccessToken());
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge((int) (jwtProvider.getAccessTokenExpiration() / 1000));

        Cookie refreshTokenCookie = new Cookie("refreshToken", tokenDto.getRefreshToken());
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge((int) (jwtProvider.getRefreshTokenExpiration() / 1000));

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);
    }

    /**
     * Bearer Prefix를 포함한 값을 전달받으면 토큰만을 추출하여 반환
     *
     * @param bearerToken
     * @return Token (String)
     */
    public String getTokenFromBearer(String bearerToken) {
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.split(" ")[1];
        }
        return null;
    }
}
