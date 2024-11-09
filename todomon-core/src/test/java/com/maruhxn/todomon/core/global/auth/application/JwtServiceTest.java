package com.maruhxn.todomon.core.global.auth.application;

import com.maruhxn.todomon.core.domain.auth.dao.RefreshTokenRepository;
import com.maruhxn.todomon.core.domain.auth.domain.RefreshToken;
import com.maruhxn.todomon.core.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.global.auth.application.JwtProvider;
import com.maruhxn.todomon.core.global.auth.application.JwtService;
import com.maruhxn.todomon.core.global.auth.dto.MemberDTO;
import com.maruhxn.todomon.core.global.auth.dto.TokenDto;
import com.maruhxn.todomon.core.global.auth.model.Role;
import com.maruhxn.todomon.core.global.auth.model.TodomonOAuth2User;
import com.maruhxn.todomon.core.global.auth.model.provider.OAuth2Provider;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.NotFoundException;
import com.maruhxn.todomon.core.global.error.exception.UnauthorizedException;
import com.maruhxn.todomon.util.IntegrationTestSupport;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import static com.maruhxn.todomon.core.global.common.Constants.ACCESS_TOKEN_HEADER;
import static com.maruhxn.todomon.core.global.common.Constants.REFRESH_TOKEN_HEADER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("[Service] - JwtService")
class JwtServiceTest extends IntegrationTestSupport {

    @Autowired
    JwtService jwtService;

    @Autowired
    JwtProvider jwtProvider;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @DisplayName("유저 이메일을 바탕으로 조회한 refresh token이 없다면 새롭게 저장한다.")
    @Test
    void saveRefreshToken() {
        // Given
        Member member = createMember();
        TodomonOAuth2User todomonOAuth2User = TodomonOAuth2User.from(MemberDTO.from(member));

        TokenDto tokenDto = TokenDto.builder()
                .accessToken("accessToken")
                .refreshToken("refreshToken")
                .build();

        // When
        jwtService.saveRefreshToken(todomonOAuth2User, tokenDto);

        // Then
        Optional<RefreshToken> optionalRefreshToken = refreshTokenRepository.findByEmail(todomonOAuth2User.getEmail());
        assertThat(optionalRefreshToken.isPresent()).isTrue();
    }

    @DisplayName("유저 정보를 바탕으로 조회한 refresh token이 있다면 전달받은 값으로 덮어씌운다.")
    @Test
    void updateRefreshToken() {
        // Given
        RefreshToken refreshToken = RefreshToken.builder()
                .email("test@test.com")
                .payload("refreshToken")
                .build();
        refreshTokenRepository.save(refreshToken);

        Member member = createMember();
        TodomonOAuth2User todomonOAuth2User = TodomonOAuth2User.from(MemberDTO.from(member));

        TokenDto tokenDto = TokenDto.builder()
                .accessToken("accessToken")
                .refreshToken("newRefreshToken")
                .build();

        // When
        jwtService.saveRefreshToken(todomonOAuth2User, tokenDto);

        // Then
        RefreshToken findRefreshToken = refreshTokenRepository.findByEmail(todomonOAuth2User.getEmail()).get();
        assertThat(findRefreshToken.getPayload()).isEqualTo(tokenDto.getRefreshToken());
    }

    @DisplayName("refresh token이 유효하다면 access token과 refresh token을 새로 발급한다.")
    @Test
    void refresh() {
        // Given
        HttpServletResponse response = new MockHttpServletResponse();
        Member member = createMember();
        TodomonOAuth2User todomonOAuth2User = TodomonOAuth2User.from(MemberDTO.from(member));

        String rawRefreshToken = jwtProvider.generateRefreshToken(todomonOAuth2User.getEmail(), new Date());
        RefreshToken refreshToken = RefreshToken.builder()
                .email(member.getEmail())
                .payload(rawRefreshToken)
                .build();
        refreshTokenRepository.save(refreshToken);

        String bearerRefreshToken = JwtProvider.BEARER_PREFIX + rawRefreshToken;
        // When
        TokenDto tokenDto = jwtService.refresh(bearerRefreshToken, response);
        // Then
        assertThat(response.getHeader(ACCESS_TOKEN_HEADER)).isEqualTo(JwtProvider.BEARER_PREFIX + tokenDto.getAccessToken());
        assertThat(response.getHeader(REFRESH_TOKEN_HEADER)).isEqualTo(JwtProvider.BEARER_PREFIX + tokenDto.getRefreshToken());
    }

    @DisplayName("refreshToken이 만료되었다면 401 에러를 반환한다.")
    @Test
    void refreshWithInvalidRefreshToken() {
        // Given
        HttpServletResponse response = new MockHttpServletResponse();
        Member member = createMember();
        TodomonOAuth2User todomonOAuth2User = TodomonOAuth2User.from(MemberDTO.from(member));
        LocalDateTime now = LocalDateTime.of(2024, 1, 18, 10, 0);
        String rawRefreshToken = jwtProvider.generateRefreshToken(
                todomonOAuth2User.getEmail(),
                Date.from(now.atZone(ZoneId.systemDefault()).toInstant())
        );

        RefreshToken refreshToken = RefreshToken.builder()
                .email(member.getEmail())
                .payload(rawRefreshToken)
                .build();
        refreshTokenRepository.save(refreshToken);

        String bearerRefreshToken = JwtProvider.BEARER_PREFIX + rawRefreshToken;
        // When / Then
        assertThatThrownBy(() -> jwtService.refresh(bearerRefreshToken, response))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("기한이 만료된 토큰입니다.");
    }

    @DisplayName("refreshToken이 데이터베이스 존재하지 않다면 에러를 반환한다.")
    @Test
    void refreshFailWhenNoRefreshToken() {
        // Given
        HttpServletResponse response = new MockHttpServletResponse();
        Member member = createMember();
        TodomonOAuth2User todomonOAuth2User = TodomonOAuth2User.from(MemberDTO.from(member));

        String rawRefreshToken = jwtProvider.generateRefreshToken(todomonOAuth2User.getEmail(), new Date());

        String bearerRefreshToken = JwtProvider.BEARER_PREFIX + rawRefreshToken;
        // When / Then
        assertThatThrownBy(() -> jwtService.refresh(bearerRefreshToken, response))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(ErrorCode.NOT_FOUND_REFRESH_TOKEN.getMessage());
    }

    @DisplayName("refreshToken에 대응되는 유저 정보가 데이터베이스 존재하지 않다면 에러를 반환한다.")
    @Test
    void refreshFailWhenNoMember() {
        // Given
        HttpServletResponse response = new MockHttpServletResponse();

        String email = "unknown@email.com";
        String rawRefreshToken = jwtProvider.generateRefreshToken(email, new Date());

        RefreshToken refreshToken = RefreshToken.builder()
                .email(email)
                .payload(rawRefreshToken)
                .build();
        refreshTokenRepository.save(refreshToken);

        String bearerRefreshToken = JwtProvider.BEARER_PREFIX + rawRefreshToken;
        // When / Then
        assertThatThrownBy(() -> jwtService.refresh(bearerRefreshToken, response))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(ErrorCode.NOT_FOUND_MEMBER.getMessage());
    }

    private Member createMember() {
        Member existingMember = Member.builder()
                .username("tester")
                .email("test@test.com")
                .role(Role.ROLE_USER)
                .providerId("google_foobarfoobar")
                .provider(OAuth2Provider.GOOGLE)
                .profileImageUrl("google-user-picutre-url")
                .build();
        return memberRepository.save(existingMember);
    }
}