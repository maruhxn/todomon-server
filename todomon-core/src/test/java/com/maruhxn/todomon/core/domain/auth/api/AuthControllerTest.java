package com.maruhxn.todomon.core.domain.auth.api;

import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.global.auth.model.Role;
import com.maruhxn.todomon.core.global.auth.model.TodomonOAuth2User;
import com.maruhxn.todomon.core.global.auth.model.provider.OAuth2Provider;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.util.ControllerTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

import static com.maruhxn.todomon.core.global.common.Constants.REFRESH_TOKEN_HEADER;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("[Controller] - AuthController")
class AuthControllerTest extends ControllerTestSupport {

    @MockBean
    TodomonOAuth2User todomonOAuth2User;

    @Test
    @DisplayName("GET /api/auth 요청 시 유저 정보를 반환한다.")
    @WithMockUser
    void getUserInfo() throws Exception {
        // Given
        Member member = Member.builder()
                .username("John Doe")
                .email("john.doe@example.com")
                .role(Role.ROLE_USER)
                .provider(OAuth2Provider.GOOGLE)
                .providerId("providerId")
                .profileImageUrl(null)
                .build();

        BDDMockito.given(todomonOAuth2User.getMember()).willReturn(member);

        // When / Then
        mockMvc.perform(
                        get("/api/auth")
                                .with(SecurityMockMvcRequestPostProcessors.authentication((
                                        new TestingAuthenticationToken(todomonOAuth2User, null, "ROLE_USER")
                                        )))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("OK"))
                .andExpect(jsonPath("message").value("유저 정보 반환"))
                .andExpect(jsonPath("data.username").value("John Doe"))
                .andExpect(jsonPath("data.email").value("john.doe@example.com"))
                .andExpect(jsonPath("data.role").value("ROLE_USER"))
                .andExpect(jsonPath("data.profileImage").isEmpty())
                .andDo(print());
    }

    @Test
    @DisplayName("Access Token의 refresh 성공 시 200을 반환한다.")
    @WithMockUser
    void refresh() throws Exception {
        // When / Then
        mockMvc.perform(
                        get("/api/auth/refresh")
                                .header(REFRESH_TOKEN_HEADER, "bearerRefreshToken")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("OK"))
                .andExpect(jsonPath("message").value("Token Refresh 성공"))
                .andDo(print());
    }

    @DisplayName("refresh 시도할 때 refresh token을 넘겨주지 않은 경우 400을 반환한다.")
    @Test
    @WithMockUser
    void refreshWithoutRefreshToken() throws Exception {
        // When / Then
        mockMvc.perform(
                        get("/api/auth/refresh")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("code").value(ErrorCode.EMPTY_REFRESH_TOKEN.name()))
                .andExpect(jsonPath("message").value(ErrorCode.EMPTY_REFRESH_TOKEN.getMessage()));
    }
}