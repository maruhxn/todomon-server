package com.maruhxn.todomon.global.auth.application;

import com.maruhxn.todomon.global.auth.dto.TokenDto;
import com.maruhxn.todomon.global.auth.model.TodomonOAuth2User;
import com.maruhxn.todomon.global.auth.model.provider.OAuth2Provider;
import com.maruhxn.todomon.global.error.ErrorCode;
import com.maruhxn.todomon.global.error.exception.UnauthorizedException;
import io.jsonwebtoken.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;

import static com.maruhxn.todomon.global.common.Constants.ACCESS_TOKEN_HEADER;
import static com.maruhxn.todomon.global.common.Constants.REFRESH_TOKEN_HEADER;

@Component
public class JwtProvider {

    @Value("${jwt.access-token.expiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-token.expiration}")
    private Long refreshTokenExpiration;

    private SecretKey secretKey;
    private JwtParser jwtParser;
    public static final String BEARER_PREFIX = "Bearer ";

    public JwtProvider(@Value("${jwt.secret-key}") String secretKey) {
        this.secretKey = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
        this.jwtParser = Jwts.parser()
                .verifyWith(this.secretKey)
                .build();
    }

    public TokenDto createJwt(TodomonOAuth2User todomonOAuth2User) {

        String email = todomonOAuth2User.getEmail();

        String accessToken = generateAccessToken(todomonOAuth2User, new Date());
        String refreshToken = generateRefreshToken(email, new Date());

        return TokenDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public String generateAccessToken(TodomonOAuth2User todomonOAuth2User, Date now) {
        String email = todomonOAuth2User.getEmail();
        String username = todomonOAuth2User.getName();
        OAuth2Provider provider = todomonOAuth2User.getProvider();
        ArrayList<? extends GrantedAuthority> authorities =
                (ArrayList<? extends GrantedAuthority>) todomonOAuth2User.getAuthorities();
        return Jwts.builder()
                .subject(email)
                .claim("username", username)
                .claim("provider", provider.name())
                .claim("role", authorities.get(0).getAuthority())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessTokenExpiration))
                .signWith(secretKey)
                .compact();
    }

    public String generateRefreshToken(String email, Date now) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + refreshTokenExpiration))
                .signWith(secretKey)
                .compact();
    }

    public void setHeader(HttpServletResponse response, TokenDto tokenDto) {
        response.addHeader(ACCESS_TOKEN_HEADER, BEARER_PREFIX + tokenDto.getAccessToken());
        response.addHeader(REFRESH_TOKEN_HEADER, BEARER_PREFIX + tokenDto.getRefreshToken());
    }

    /**
     * 요청의 인증 헤더에 접근하여 accessToken을 추출
     *
     * @param request
     * @return 추출된 token
     */
    public String resolveAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(ACCESS_TOKEN_HEADER);
        return getTokenFromBearer(bearerToken);
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

    /**
     * 서명된 토큰 값을 파싱하여 payload를 추출
     *
     * @param token
     * @return claims(payload)
     */
    public Claims getPayload(String token) {
        return jwtParser
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * token을 전달받아 해당 토큰이 유효한지 검증
     *
     * @param token
     */
    public void validate(String token) {
        try {
            getPayload(token)
                    .getExpiration()
                    .after(new Date());
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
