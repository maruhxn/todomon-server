package com.maruhxn.todomon.core.domain.social.api;

import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.member.domain.TitleName;
import com.maruhxn.todomon.core.domain.social.dao.FollowRepository;
import com.maruhxn.todomon.core.domain.social.domain.Follow;
import com.maruhxn.todomon.core.domain.social.domain.FollowRequestStatus;
import com.maruhxn.todomon.core.global.auth.model.Role;
import com.maruhxn.todomon.core.global.auth.model.provider.OAuth2Provider;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.util.ControllerIntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static com.maruhxn.todomon.core.domain.social.domain.FollowRequestStatus.ACCEPTED;
import static com.maruhxn.todomon.core.domain.social.domain.FollowRequestStatus.REJECTED;
import static com.maruhxn.todomon.core.global.auth.application.JwtProvider.BEARER_PREFIX;
import static com.maruhxn.todomon.core.global.common.Constants.ACCESS_TOKEN_HEADER;
import static com.maruhxn.todomon.core.global.common.Constants.REFRESH_TOKEN_HEADER;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("[Integration] - Follow")
class FollowIntegrationTest extends ControllerIntegrationTestSupport {

    static final String FOLLOW_BASE_URL = "/api/social/follows";

    @Autowired
    FollowRepository followRepository;

    // 팔로우 요청 조회
    @Test
    @DisplayName("GET /api/social/follows/requests/pending - 자신에게 수신된 팔로우 요청 목록을 페이징 조회한다.")
    void getFollowRequets() throws Exception {
        // given
        List<Member> followerList = IntStream.range(1, 20)
                .mapToObj(i -> createMember("tester" + i))
                .toList();

        List<Follow> followRequests = new ArrayList<>();
        followerList.forEach(follower -> {
            Follow follow = Follow.builder()
                    .follower(follower)
                    .followee(member)
                    .build();
            followRequests.add(follow);
        });
        followRepository.saveAll(followRequests);

        // when / then
        mockMvc.perform(
                        get(FOLLOW_BASE_URL + "/requests/pending")
                                .queryParam("pageNumber", String.valueOf(0))
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("OK"))
                .andExpect(jsonPath("message").value("팔로우 요청 조회 성공"))
                .andExpect(jsonPath("data.results.size()").value(10));
    }

//    // 맞팔로우
//    @Test
//    @DisplayName("POST /api/social/follows/{followerId}/mutual - 맞팔로우를 한다")
//    void matFollow() throws Exception {
//        // given
//        Member tester1 = createMember("tester1");
//        Follow follow = Follow.builder()
//                .follower(tester1)
//                .followee(member)
//                .build();
//        follow.updateStatus(ACCEPTED);
//        followRepository.save(follow);
//
//        // when / then
//        mockMvc.perform(
//                        post(FOLLOW_BASE_URL + "/{followerId}/mutual", tester1.getId())
//                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
//                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
//                )
//                .andExpect(status().isCreated());
//
//    }

    @Test
    @DisplayName("POST /api/social/follows/{memberId} - 유저에게 팔로우 요청을 보낸다")
    void followRequest() throws Exception {

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
    @DisplayName("PATCH /api/social/follows/requests/{followId}/respond - 팔로우 요청에 응답한다.")
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
                        patch(FOLLOW_BASE_URL + "/requests/{followId}/respond", follow.getId())
                                .queryParam("isAccepted", String.valueOf(true))
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PATCH /api/social/follows/requests/{followId}/respond - 본인에 대한 팔로우 요청이 아닌 경우 응답 요청 시 403 에러를 반환한다.")
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
                        patch(FOLLOW_BASE_URL + "/requests/{followId}/respond", follow.getId())
                                .queryParam("isAccepted", String.valueOf(true))
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value(ErrorCode.FORBIDDEN.name()))
                .andExpect(jsonPath("message").value(ErrorCode.FORBIDDEN.getMessage()));
    }

    @Test
    @DisplayName("DELETE /api/social/follows/{followerId}/remove} - 팔로위가 팔로워를 대상으로 팔로우를 끊는다")
    void removeFollower() throws Exception {
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
                        delete(FOLLOW_BASE_URL + "/{followerId}/remove", tester1.getId())
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/social/follows/{followeeId}/unfollow - 팔로워가 팔로위를 대상으로 팔로우를 끊는다")
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
                        delete(FOLLOW_BASE_URL + "/{followeeId}/unfollow", tester1.getId())
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/social/follows/{memberId}/followers - 해당 유저의 팔로워 리스트를 조회한다")
    void getFollowers() throws Exception {
        // given
        Member tester1 = createMember("tester1");
        Member tester2 = createMember("tester2");
        Member tester3 = createMember("tester3");

        TitleName titleName = createTitleName(tester3);

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
                .andExpect(jsonPath("data.results[0].followerId").value(tester3.getId()))
                .andExpect(jsonPath("data.results[0].username").value(tester3.getUsername()))
                .andExpect(jsonPath("data.results[0].profileImageUrl").value(tester3.getProfileImageUrl()))
                .andExpect(jsonPath("data.results[0].title.name").value(titleName.getName()))
                .andExpect(jsonPath("data.results[0].title.color").value(titleName.getColor()));
    }

    @Test
    @DisplayName("GET /api/social/follows/{memberId}/followings - 해당 유저의 팔로잉 리스트를 조회한다")
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
        TitleName titleName = createTitleName(tester3);

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
                .andExpect(jsonPath("data.results[0].followeeId").value(tester3.getId()))
                .andExpect(jsonPath("data.results[0].username").value(tester3.getUsername()))
                .andExpect(jsonPath("data.results[0].profileImageUrl").value(tester3.getProfileImageUrl()))
                .andExpect(jsonPath("data.results[0].title.name").value(titleName.getName()))
                .andExpect(jsonPath("data.results[0].title.color").value(titleName.getColor()));
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