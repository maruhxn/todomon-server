package com.maruhxn.todomon.domain.social.api;

import com.maruhxn.todomon.domain.member.domain.Member;
import com.maruhxn.todomon.domain.social.dao.FollowRepository;
import com.maruhxn.todomon.domain.social.domain.Follow;
import com.maruhxn.todomon.domain.social.domain.FollowRequestStatus;
import com.maruhxn.todomon.global.auth.model.Role;
import com.maruhxn.todomon.global.auth.model.provider.OAuth2Provider;
import com.maruhxn.todomon.global.error.ErrorCode;
import com.maruhxn.todomon.util.ControllerIntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.maruhxn.todomon.domain.social.domain.FollowRequestStatus.ACCEPTED;
import static com.maruhxn.todomon.domain.social.domain.FollowRequestStatus.REJECTED;
import static com.maruhxn.todomon.global.auth.application.JwtProvider.BEARER_PREFIX;
import static com.maruhxn.todomon.global.common.Constants.ACCESS_TOKEN_HEADER;
import static com.maruhxn.todomon.global.common.Constants.REFRESH_TOKEN_HEADER;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("[Integration] - Follow")
class FollowIntegrationTest extends ControllerIntegrationTestSupport {

    static final String FOLLOW_BASE_URL = "/api/social/follow";

    @Autowired
    FollowRepository followRepository;

    @Test
    @DisplayName("POST /api/social/follow/{memberId} - 유저에게 팔로우 요청을 보낸다")
    void follow() throws Exception {
        // given
        Member tester1 = createMember("tester1");

        // when / then
        mockMvc.perform(
                        post(FOLLOW_BASE_URL + "/{memberId}", tester1.getId())
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("PATCH /api/social/follow/accept/{followId} - 팔로우 요청에 응답한다.")
    void respondFollow() throws Exception {
        // given
        Member tester1 = createMember("tester1");
        Follow follow = Follow.builder()
                .follower(tester1)
                .followee(member)
                .build();
        followRepository.save(follow);

        // when / then
        mockMvc.perform(
                        patch(FOLLOW_BASE_URL + "/accept/{followId}", follow.getId())
                                .queryParam("isAccepted", String.valueOf(true))
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PATCH /api/social/follow/accept/{followId} - 본인에 대한 팔로우 요청이 아닌 경우 응답 요청 시 403 에러를 반환한다.")
    void respondFollowReturn403() throws Exception {
        // given
        Member tester1 = createMember("tester1");
        Follow follow = Follow.builder()
                .follower(member)
                .followee(tester1)
                .build();
        followRepository.save(follow);

        // when / then
        mockMvc.perform(
                        patch(FOLLOW_BASE_URL + "/accept/{followId}", follow.getId())
                                .queryParam("isAccepted", String.valueOf(true))
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value(ErrorCode.FORBIDDEN.name()))
                .andExpect(jsonPath("message").value(ErrorCode.FORBIDDEN.getMessage()));
    }

    @Test
    @DisplayName("DELETE /api/social/follow/{memberId} - 팔로위가 팔로워를 대상으로 팔로우를 끊는다")
    void unfollowToFollower() throws Exception {
        // given
        Member tester1 = createMember("tester1");
        Follow follow = Follow.builder()
                .follower(tester1)
                .followee(member)
                .build();
        follow.updateStatus(FollowRequestStatus.ACCEPTED);
        followRepository.save(follow);

        // when / then
        mockMvc.perform(
                        delete(FOLLOW_BASE_URL + "/{memberId}", tester1.getId())
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/social/follow/{memberId} - 팔로워가 팔로위를 대상으로 팔로우를 끊는다")
    void unfollowToFollowee() throws Exception {
        // given
        Member tester1 = createMember("tester1");
        Follow follow = Follow.builder()
                .follower(member)
                .followee(tester1)
                .build();
        follow.updateStatus(FollowRequestStatus.ACCEPTED);
        followRepository.save(follow);

        // when / then
        mockMvc.perform(
                        delete(FOLLOW_BASE_URL + "/{memberId}", tester1.getId())
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/social/follow/{memberId}/followers - 해당 유저의 팔로우 리스트를 조회한다")
    void getFollowers() throws Exception {
        // given
        Member tester1 = createMember("tester1");
        Member tester2 = createMember("tester2");
        Member tester3 = createMember("tester3");

        Follow follow1 = Follow.builder()
                .follower(tester1)
                .followee(member)
                .build();
        Follow follow2 = Follow.builder()
                .follower(tester2)
                .followee(member)
                .build();

        Follow follow3 = Follow.builder()
                .follower(tester3)
                .followee(member)
                .build();

        follow1.updateStatus(ACCEPTED);
        follow2.updateStatus(REJECTED);
        follow3.updateStatus(ACCEPTED);
        followRepository.saveAll(List.of(follow1, follow2, follow3));

        // when / then
        mockMvc.perform(
                        get(FOLLOW_BASE_URL + "/{memberId}/followers", member.getId())
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("OK"))
                .andExpect(jsonPath("message").value("팔로워 리스트 조회 성공"))
                .andExpect(jsonPath("data").isArray())
                .andExpect(jsonPath("data[0].id").value(tester3.getId()))
                .andExpect(jsonPath("data[0].username").value(tester3.getUsername()))
                .andExpect(jsonPath("data[0].profileImageUrl").value(tester3.getProfileImageUrl()));
    }

    @Test
    @DisplayName("GET /api/social/follow/{memberId}/followings - 해당 유저의 팔로잉 리스트를 조회한다")
    void getFollowings() throws Exception {
        // given
        Member tester1 = createMember("tester1");
        Member tester2 = createMember("tester2");
        Member tester3 = createMember("tester3");

        Follow follow1 = Follow.builder()
                .follower(member)
                .followee(tester1)
                .build();
        Follow follow2 = Follow.builder()
                .follower(member)
                .followee(tester2)
                .build();

        Follow follow3 = Follow.builder()
                .follower(member)
                .followee(tester3)
                .build();

        follow1.updateStatus(ACCEPTED);
        follow2.updateStatus(REJECTED);
        follow3.updateStatus(ACCEPTED);
        followRepository.saveAll(List.of(follow1, follow2, follow3));

        // when / then
        mockMvc.perform(
                        get(FOLLOW_BASE_URL + "/{memberId}/followings", member.getId())
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("OK"))
                .andExpect(jsonPath("message").value("팔로잉 리스트 조회 성공"))
                .andExpect(jsonPath("data").isArray())
                .andExpect(jsonPath("data[0].id").value(tester3.getId()))
                .andExpect(jsonPath("data[0].username").value(tester3.getUsername()))
                .andExpect(jsonPath("data[0].profileImageUrl").value(tester3.getProfileImageUrl()));
    }

    private Member createMember(String username) {
        Member member = Member.builder()
                .username(username)
                .email(username + "@test.com")
                .provider(OAuth2Provider.GOOGLE)
                .providerId("google_" + username)
                .role(Role.ROLE_USER)
                .profileImageUrl("profileImageUrl")
                .build();
        member.initDiligence();
        return memberRepository.save(member);
    }
}