package com.maruhxn.todomon.core.global.auth.application.model;

import com.maruhxn.todomon.core.global.auth.model.provider.GoogleUser;
import com.maruhxn.todomon.core.global.auth.model.provider.KakaoUser;
import com.maruhxn.todomon.core.global.auth.model.provider.NaverUser;
import com.maruhxn.todomon.core.global.auth.model.provider.OAuth2ProviderUser;
import com.maruhxn.todomon.core.global.error.exception.BadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@DisplayName("[Service] - TodomonOAuth2UserService")
class OAuth2ProviderUserTest {

    @Test
    void create_shouldReturnGoogleUser_whenProviderIsGoogle() {
        // Given
        String registrationId = "google";
        Map<String, Object> attributes = Map.of(
                "sub", "12345",
                "email", "user@example.com",
                "name", "Test User",
                "picture", "https://example.com/profile.jpg"
        );

        // When
        OAuth2ProviderUser user = OAuth2ProviderUser.create(attributes, registrationId);

        // Then
        assertThat(user.getProvider()).isEqualTo("google");
        assertThat(user.getEmail()).isEqualTo("user@example.com");
        assertThat(user.getUsername()).isEqualTo("Test User");
        assertThat(user.getProviderId()).isEqualTo("google_12345");
        assertThat(user.getProfileImageUrl()).isEqualTo("https://example.com/profile.jpg");
        assertThat(user instanceof GoogleUser).isTrue();
    }

    @Test
    void create_shouldReturnNaverUser_whenProviderIsNaver() {
        // Given
        String registrationId = "naver";

        Map<String, Object> response = Map.of(
                "id", "54321",
                "email", "naveruser@example.com",
                "name", "Naver User",
                "profile_image", "https://example.com/naver-profile.jpg"
        );

        Map<String, Object> attributes = Map.of("response", response);

        // When
        OAuth2ProviderUser user = OAuth2ProviderUser.create(attributes, registrationId);

        // Then
        assertThat(user.getProvider()).isEqualTo("naver");
        assertThat(user.getEmail()).isEqualTo("naveruser@example.com");
        assertThat(user.getUsername()).isEqualTo("Naver User");
        assertThat(user.getProviderId()).isEqualTo("naver_54321");
        assertThat(user.getProfileImageUrl()).isEqualTo("https://example.com/naver-profile.jpg");
        assertThat(user instanceof NaverUser).isTrue();
    }

    @Test
    void create_shouldReturnKakaoUser_whenProviderIsKakao() {
        // Given
        String registrationId = "kakao";
        Map<String, Object> attributes = Map.of(
                "id", "67890",
                "kakao_account", Map.of(
                        "email", "kakaouser@example.com",
                        "profile", Map.of(
                                "nickname", "Kakao User",
                                "profile_image_url", "https://example.com/kakao-profile.jpg"
                        )
                )
        );

        // When
        OAuth2ProviderUser user = OAuth2ProviderUser.create(attributes, registrationId);

        // Then
        assertThat(user.getProvider()).isEqualTo("kakao");
        assertThat(user.getEmail()).isEqualTo("kakaouser@example.com");
        assertThat(user.getUsername()).isEqualTo("Kakao User");
        assertThat(user.getProviderId()).isEqualTo("kakao_67890");
        assertThat(user.getProfileImageUrl()).isEqualTo("https://example.com/kakao-profile.jpg");
        assertThat(user instanceof KakaoUser).isTrue();
    }

    @Test
    void create_shouldThrowException_whenProviderIsUnknown() {
        // Given
        String registrationId = "unknown";
        Map<String, Object> attributes = Map.of();

        // When & Then
        assertThatThrownBy(() -> OAuth2ProviderUser.create(attributes, registrationId))
                .hasMessage("일치하는 제공자가 없습니다.")
                .isInstanceOf(BadRequestException.class);
    }
}