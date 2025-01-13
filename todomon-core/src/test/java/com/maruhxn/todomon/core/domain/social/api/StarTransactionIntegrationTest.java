package com.maruhxn.todomon.core.domain.social.api;

import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.social.dao.FollowRepository;
import com.maruhxn.todomon.core.domain.social.dao.StarTransactionRepository;
import com.maruhxn.todomon.core.domain.social.domain.Follow;
import com.maruhxn.todomon.core.domain.social.domain.FollowRequestStatus;
import com.maruhxn.todomon.core.domain.social.domain.StarTransaction;
import com.maruhxn.todomon.core.domain.social.domain.StarTransactionStatus;
import com.maruhxn.todomon.core.global.auth.model.Role;
import com.maruhxn.todomon.core.global.auth.model.provider.OAuth2Provider;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.util.ControllerIntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.maruhxn.todomon.core.global.auth.application.JwtProvider.BEARER_PREFIX;
import static com.maruhxn.todomon.core.global.common.Constants.ACCESS_TOKEN_HEADER;
import static com.maruhxn.todomon.core.global.common.Constants.REFRESH_TOKEN_HEADER;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("[Integration] - Star")
class StarTransactionIntegrationTest extends ControllerIntegrationTestSupport {

    static final String STAR_TRANSACTION_BASE_URL = "/api/social/stars";

    @Autowired
    StarTransactionRepository starTransactionRepository;

    @Autowired
    FollowRepository followRepository;

    @Test
    @DisplayName("POST /api/social/stars/send/{memberId} - 내가 팔로우하고 있는 유저에게 별을 보낸다.")
    void sendStar() throws Exception {
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
                        post(STAR_TRANSACTION_BASE_URL + "/send/{memberId}", tester1.getId())
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("code").value("OK"))
                .andExpect(jsonPath("message").value("별 보내기 성공 - 수신자 아이디: " + tester1.getId()));
    }

    @Test
    @DisplayName("POST /api/social/stars/send/{memberId} - 팔로우하고 있지 않은 유저에게 별을 보내면 403 에러를 반환한다.")
    void sendStarReturn403() throws Exception {
        // given
        Member tester1 = createMember("tester1");
        Follow follow = Follow.builder()
                .follower(member)
                .followee(tester1)
                .build();
        followRepository.save(follow);

        // when / then
        mockMvc.perform(
                        post(STAR_TRANSACTION_BASE_URL + "/send/{memberId}", tester1.getId())
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value(ErrorCode.NOT_ACCEPTED_FOLLOW.name()))
                .andExpect(jsonPath("message").value(ErrorCode.NOT_ACCEPTED_FOLLOW.getMessage()));
    }

    @Test
    @DisplayName("POST /api/social/stars/send/{memberId} - 수신자 정보가 존재하지 않으면 404 에러를 반환한다.")
    void sendStarReturn404() throws Exception {
        // given

        // when / then
        mockMvc.perform(
                        post(STAR_TRANSACTION_BASE_URL + "/send/{memberId}", 0)
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("code").value(ErrorCode.NOT_FOUND_MEMBER.name()))
                .andExpect(jsonPath("message").value("수신자 정보가 존재하지 않습니다."));
    }

    @Test
    @DisplayName("PATCH /api/social/stars/receive/{transactionId} - 수신된 별을 1개 받는다.")
    void receiveStar() throws Exception {
        // given
        Member tester1 = createMember("tester1");
        Follow follow = Follow.builder()
                .follower(member)
                .followee(tester1)
                .build();
        follow.updateStatus(FollowRequestStatus.ACCEPTED);
        followRepository.save(follow);

        StarTransaction transaction = StarTransaction.createTransaction(tester1, member);
        starTransactionRepository.save(transaction);

        // when / then
        mockMvc.perform(
                        patch(STAR_TRANSACTION_BASE_URL + "/receive/{transactionId}", transaction.getId())
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PATCH /api/social/stars/receive/{transactionId} - 이미 받은 별을 다시 받으면 400 에러를 반환한다.")
    void receiveStarReturn400() throws Exception {
        // given
        Member tester1 = createMember("tester1");
        Follow follow = Follow.builder()
                .follower(member)
                .followee(tester1)
                .build();
        follow.updateStatus(FollowRequestStatus.ACCEPTED);
        followRepository.save(follow);

        StarTransaction transaction = StarTransaction.createTransaction(tester1, member);
        transaction.updateStatus(StarTransactionStatus.RECEIVED);
        starTransactionRepository.save(transaction);

        // when / then
        mockMvc.perform(
                        patch(STAR_TRANSACTION_BASE_URL + "/receive/{transactionId}", transaction.getId())
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("code").value(ErrorCode.ALREADY_RECEIVED.name()))
                .andExpect(jsonPath("message").value(ErrorCode.ALREADY_RECEIVED.getMessage()));
    }

    @Test
    @DisplayName("PATCH /api/social/stars/receiveAll - 수신된 모든 별을 받는다.")
    void receiveAllStars() throws Exception {
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
        follow1.updateStatus(FollowRequestStatus.ACCEPTED);
        follow2.updateStatus(FollowRequestStatus.ACCEPTED);
        follow3.updateStatus(FollowRequestStatus.ACCEPTED);
        followRepository.saveAll(List.of(follow1, follow2, follow3));

        StarTransaction transaction1 = StarTransaction.createTransaction(tester1, member);
        StarTransaction transaction2 = StarTransaction.createTransaction(tester2, member);
        StarTransaction transaction3 = StarTransaction.createTransaction(tester3, member);
        starTransactionRepository.saveAll(List.of(transaction1, transaction2, transaction3));

        // when / then
        mockMvc.perform(
                        patch(STAR_TRANSACTION_BASE_URL + "/receiveAll")
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isNoContent());
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