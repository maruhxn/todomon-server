package com.maruhxn.todomon.global.auth.application;

import com.maruhxn.todomon.domain.auth.dao.RefreshTokenRepository;
import com.maruhxn.todomon.domain.auth.domain.RefreshToken;
import com.maruhxn.todomon.domain.member.application.MemberService;
import com.maruhxn.todomon.domain.member.domain.Member;
import com.maruhxn.todomon.global.auth.dto.TokenDto;
import com.maruhxn.todomon.global.auth.model.TodomonOAuth2User;
import com.maruhxn.todomon.global.error.ErrorCode;
import com.maruhxn.todomon.global.error.exception.NotFoundException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class JwtService {
    private final MemberService memberService;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public TodomonOAuth2User getPrincipal(String accessToken) {
        Claims payload = jwtProvider.getPayload(accessToken);
        String email = payload.getSubject();
        Member findMember = memberService.findMemberByEmail(email);
        return TodomonOAuth2User.of(findMember);
    }

    public void logout(String bearerRefreshToken) {
        String refreshToken = jwtProvider.getTokenFromBearer(bearerRefreshToken);
        String email = jwtProvider.getPayload(refreshToken).getSubject();
        refreshTokenRepository.deleteAllByEmail(email);
    }

    public void saveRefreshToken(TodomonOAuth2User todomonOAuth2User, TokenDto tokenDto) {
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByEmail(todomonOAuth2User.getEmail());
        refreshToken.ifPresentOrElse(
                // 있다면 새토큰 발급후 업데이트
                token -> {
                    token.updatePayload(tokenDto.getRefreshToken());
                },
                // 없다면 새로 만들고 DB에 저장
                () -> {
                    RefreshToken newToken =
                            new RefreshToken(tokenDto.getRefreshToken(), todomonOAuth2User.getEmail());
                    refreshTokenRepository.save(newToken);
                });
    }

    public TokenDto refresh(String bearerRefreshToken, HttpServletResponse response) {
        String refreshToken = jwtProvider.getTokenFromBearer(bearerRefreshToken);

        jwtProvider.validate(refreshToken);

        RefreshToken findRefreshToken = refreshTokenRepository.findByPayload(refreshToken)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_REFRESH_TOKEN));

        Member findMember = memberService.findMemberByEmail(findRefreshToken.getEmail());

        // access token 과 refresh token 모두를 재발급
        TodomonOAuth2User todomonOAuth2User = TodomonOAuth2User.of(findMember);

        String newAccessToken = jwtProvider.generateAccessToken(todomonOAuth2User, new Date());
        String newRefreshToken = jwtProvider.generateRefreshToken(todomonOAuth2User.getEmail(), new Date());

        TokenDto tokenDto = TokenDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();

        this.saveRefreshToken(todomonOAuth2User, tokenDto);

        jwtProvider.setHeader(response, tokenDto);

        return tokenDto;
    }
}
