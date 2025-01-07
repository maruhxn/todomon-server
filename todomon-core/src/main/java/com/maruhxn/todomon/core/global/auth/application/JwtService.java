package com.maruhxn.todomon.core.global.auth.application;

import com.maruhxn.todomon.core.domain.auth.dao.RefreshTokenRepository;
import com.maruhxn.todomon.core.domain.auth.domain.RefreshToken;
import com.maruhxn.todomon.core.domain.member.application.MemberService;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.global.auth.dto.MemberDTO;
import com.maruhxn.todomon.core.global.auth.dto.TokenDto;
import com.maruhxn.todomon.core.global.auth.model.TodomonOAuth2User;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.NotFoundException;
import com.maruhxn.todomon.core.global.error.exception.UnauthorizedException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.maruhxn.todomon.core.global.auth.application.JwtProvider.BEARER_PREFIX;
import static com.maruhxn.todomon.core.global.common.Constants.ACCESS_TOKEN_HEADER;
import static com.maruhxn.todomon.core.global.common.Constants.REFRESH_TOKEN_HEADER;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final MemberService memberService;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public TodomonOAuth2User getPrincipal(String accessToken) {
        return TodomonOAuth2User.from(jwtProvider.extractMemberDTOFromAccessToken(accessToken));
    }

    @Transactional
    public void logout(String bearerRefreshToken) {
        String email = this.extractEmailFromRefreshToken(bearerRefreshToken);
        refreshTokenRepository.deleteAllByEmail(email);
    }

    private String extractEmailFromRefreshToken(String bearerRefreshToken) {
        String refreshToken = jwtProvider.getTokenFromBearer(bearerRefreshToken);
        return jwtProvider.getEmail(refreshToken);
    }

    @Transactional
    public TokenDto tokenRefresh(String bearerRefreshToken, HttpServletResponse response) {
        String refreshToken = jwtProvider.getTokenFromBearer(bearerRefreshToken);

        this.validate(refreshToken);

        TodomonOAuth2User todomonOAuth2User = this.extractTodomonOAuth2User(refreshToken);

        return this.doTokenGenerationProcess(response, todomonOAuth2User);
    }

    private TodomonOAuth2User extractTodomonOAuth2User(String refreshToken) {
        RefreshToken findRefreshToken = refreshTokenRepository.findByPayload(refreshToken)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_REFRESH_TOKEN));

        Member findMember = memberService.findMemberByEmail(findRefreshToken.getEmail());

        MemberDTO dto = MemberDTO.from(findMember);

        return TodomonOAuth2User.from(dto);
    }

    @Transactional
    public TokenDto doTokenGenerationProcess(HttpServletResponse response, TodomonOAuth2User principal) {
        TokenDto tokenDto = jwtProvider.createJwt(principal);
        this.saveRefreshTokenAndSetHeader(response, tokenDto);
        return tokenDto;
    }

    private void saveRefreshTokenAndSetHeader(HttpServletResponse response, TokenDto tokenDto) {
        this.upsertRefreshToken(tokenDto);
        this.setHeader(response, tokenDto);
    }

    private void upsertRefreshToken(TokenDto tokenDto) {
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByEmail(tokenDto.getEmail());
        refreshToken.ifPresentOrElse(
                // 있다면 새토큰 발급후 업데이트
                token -> {
                    token.updatePayload(tokenDto.getRefreshToken());
                },
                // 없다면 새로 만들고 DB에 저장
                () -> {
                    RefreshToken newToken =
                            new RefreshToken(tokenDto.getRefreshToken(), tokenDto.getEmail());
                    refreshTokenRepository.save(newToken);
                });
    }

    private void setHeader(HttpServletResponse response, TokenDto tokenDto) {
        response.addHeader(ACCESS_TOKEN_HEADER, BEARER_PREFIX + tokenDto.getAccessToken());
        response.addHeader(REFRESH_TOKEN_HEADER, BEARER_PREFIX + tokenDto.getRefreshToken());
    }

    public String resolveAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(ACCESS_TOKEN_HEADER);
        return jwtProvider.getTokenFromBearer(bearerToken);
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
}
