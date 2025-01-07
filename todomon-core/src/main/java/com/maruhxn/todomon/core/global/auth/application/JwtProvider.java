package com.maruhxn.todomon.core.global.auth.application;

import com.maruhxn.todomon.core.global.auth.dto.MemberDTO;
import com.maruhxn.todomon.core.global.auth.dto.TokenDto;
import com.maruhxn.todomon.core.global.auth.model.TodomonOAuth2User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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
        String email = todomonOAuth2User.getEmail();
        String accessToken = generateAccessToken(todomonOAuth2User, new Date());
        String refreshToken = generateRefreshToken(email, new Date());

        return TokenDto.builder()
                .email(email)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public String generateAccessToken(TodomonOAuth2User todomonOAuth2User, Date now) {
        Long id = todomonOAuth2User.getId();
        String email = todomonOAuth2User.getEmail();
        String username = todomonOAuth2User.getName();
        String provider = todomonOAuth2User.getProvider();
        ArrayList<? extends GrantedAuthority> authorities =
                (ArrayList<? extends GrantedAuthority>) todomonOAuth2User.getAuthorities();

        return Jwts.builder()
                .subject(email)
                .claim("id", id)
                .claim("username", username)
                .claim("provider", provider)
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

    public Long getId(String token) {
        return getPayload(token).get("id", Long.class);
    }

    public String getUsername(String token) {
        return getPayload(token).get("username", String.class);
    }

    public String getEmail(String token) {
        return getPayload(token).getSubject();
    }

    public String getProvider(String token) {
        return getPayload(token).get("provider", String.class);
    }

    public String getRole(String token) {
        return getPayload(token).get("role", String.class);
    }

    public MemberDTO extractMemberDTOFromAccessToken(String accessToken) {
        Long id = this.getId(accessToken);
        String username = this.getUsername(accessToken);
        String email = this.getEmail(accessToken);
        String role = this.getRole(accessToken);
        String provider = this.getProvider(accessToken);

        return MemberDTO.builder()
                .id(id)
                .username(username)
                .email(email)
                .role(role)
                .provider(provider)
                .build();
    }
}
