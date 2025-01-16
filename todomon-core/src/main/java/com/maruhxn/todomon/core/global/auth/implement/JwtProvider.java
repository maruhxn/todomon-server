package com.maruhxn.todomon.core.global.auth.implement;

import com.maruhxn.todomon.core.global.auth.dto.TokenDto;
import com.maruhxn.todomon.core.global.auth.dto.UserInfo;
import com.maruhxn.todomon.core.global.auth.model.TodomonOAuth2User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;

@Component
public class JwtProvider {

    public static final String BEARER_PREFIX = "Bearer ";

    @Value("${jwt.access-token.expiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-token.expiration}")
    private Long refreshTokenExpiration;

    private SecretKey secretKey;
    private JwtParser jwtParser;

    public JwtProvider(@Value("${jwt.secret-key}") String secretKey) {
        this.secretKey = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
        this.jwtParser = Jwts.parser()
                .verifyWith(this.secretKey)
                .build();
    }

    public TokenDto createJwt(TodomonOAuth2User todomonOAuth2User) {
        String accessToken = generateAccessToken(todomonOAuth2User, new Date());
        String refreshToken = generateRefreshToken(todomonOAuth2User.getName(), new Date());

        return TokenDto.builder()
                .username(todomonOAuth2User.getName())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public String generateAccessToken(TodomonOAuth2User todomonOAuth2User, Date now) {
        ArrayList<? extends GrantedAuthority> authorities =
                (ArrayList<? extends GrantedAuthority>) todomonOAuth2User.getAuthorities();

        return Jwts.builder()
                .subject(todomonOAuth2User.getName())
                .claim("id", todomonOAuth2User.getId())
                .claim("username", todomonOAuth2User.getName())
                .claim("isSubscribed", todomonOAuth2User.getIsSubscribed())
                .claim("role", authorities.get(0).getAuthority())
                .claim("profileImage", todomonOAuth2User.getProfileImage())
                .claim("starPoint", todomonOAuth2User.getStarPoint())
                .claim("foodCnt", todomonOAuth2User.getFoodCnt())
                .claim("provider", todomonOAuth2User.getProvider())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessTokenExpiration))
                .signWith(secretKey)
                .compact();
    }

    public String generateRefreshToken(String username, Date now) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + refreshTokenExpiration))
                .signWith(secretKey)
                .compact();
    }

    public Long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    public Long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    public UserInfo extractMemberDTOFromAccessToken(String accessToken) {
        return UserInfo.builder()
                .id(this.getId(accessToken))
                .username(this.getUsername(accessToken))
                .isSubscribed(this.getIsSubscribed(accessToken))
                .profileImage(this.getProfileImageUrl(accessToken))
                .starPoint(this.getStarPoint(accessToken))
                .foodCnt(this.getFoodCnt(accessToken))
                .provider(this.getProvider(accessToken))
                .role(this.getRole(accessToken))
                .provider(this.getProvider(accessToken))
                .build();
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

    public Long getId(String token) {
        return getPayload(token).get("id", Long.class);
    }

    public String getUsername(String token) {
        return getPayload(token).get("username", String.class);
    }

    public String getProvider(String token) {
        return getPayload(token).get("provider", String.class);
    }

    public String getRole(String token) {
        return getPayload(token).get("role", String.class);
    }

    private Long getStarPoint(String token) {
        return getPayload(token).get("starPoint", Long.class);
    }

    private Long getFoodCnt(String token) {
        return getPayload(token).get("foodCnt", Long.class);
    }

    private String getProfileImageUrl(String token) {
        return getPayload(token).get("profileImage", String.class);
    }

    private boolean getIsSubscribed(String token) {
        return getPayload(token).get("isSubscribed", Boolean.class);
    }
}
