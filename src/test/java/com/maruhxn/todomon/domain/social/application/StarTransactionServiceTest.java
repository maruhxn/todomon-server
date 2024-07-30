package com.maruhxn.todomon.domain.social.application;

import com.maruhxn.todomon.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.domain.member.domain.Member;
import com.maruhxn.todomon.domain.social.dao.FollowRepository;
import com.maruhxn.todomon.domain.social.dao.StarTransactionRepository;
import com.maruhxn.todomon.domain.social.domain.Follow;
import com.maruhxn.todomon.domain.social.domain.FollowRequestStatus;
import com.maruhxn.todomon.domain.social.domain.StarTransaction;
import com.maruhxn.todomon.domain.social.domain.StarTransactionStatus;
import com.maruhxn.todomon.global.auth.model.Role;
import com.maruhxn.todomon.global.auth.model.provider.OAuth2Provider;
import com.maruhxn.todomon.global.error.ErrorCode;
import com.maruhxn.todomon.global.error.exception.BadRequestException;
import com.maruhxn.todomon.global.error.exception.ForbiddenException;
import com.maruhxn.todomon.util.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("[Service] - StarService")
class StarTransactionServiceTest extends IntegrationTestSupport {

    @Autowired
    StarTransactionService starTransactionService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    StarTransactionRepository starTransactionRepository;

    @Autowired
    FollowRepository followRepository;

    @Test
    @DisplayName("팔로우 중인 유저에게 ⭐️을 보낼 수 있다.")
    void sendStar() {
        Member tester1 = createMember("tester1");
        Member tester2 = createMember("tester2");
        Follow follow = Follow.builder()
                .follower(tester1)
                .followee(tester2)
                .build();
        follow.updateStatus(FollowRequestStatus.ACCEPTED);
        followRepository.save(follow);

        // when
        starTransactionService.sendStar(tester1, tester2.getId());

        // then
        assertThat(starTransactionRepository.findAll())
                .hasSize(1)
                .first()
                .extracting("sender", "receiver", "status")
                .containsExactly(tester1, tester2, StarTransactionStatus.SENT);
    }

    @Test
    @DisplayName("맞팔로우 중이지 않은 유저에게는 ⭐️을 보낼 수 없다.")
    void sendStarFailByForbidden() {
        Member tester1 = createMember("tester1");
        Member tester2 = createMember("tester2");
        Follow follow = Follow.builder()
                .follower(tester1)
                .followee(tester2)
                .build();
        followRepository.save(follow);
        // when / then
        assertThatThrownBy(() -> starTransactionService.sendStar(tester1, tester2.getId()))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage(ErrorCode.NOT_ACCEPTED_FOLLOW.getMessage());
    }

    @Test
    @DisplayName("전달받은 ⭐️을 받는다.")
    void receiveOneStar() {
        // given
        Member tester1 = createMember("tester1");
        Member tester2 = createMember("tester2");
        Follow follow = Follow.builder()
                .follower(tester1)
                .followee(tester2)
                .build();
        follow.updateStatus(FollowRequestStatus.ACCEPTED);
        followRepository.save(follow);

        StarTransaction transaction = StarTransaction.createTransaction(tester1, tester2);
        starTransactionRepository.save(transaction);

        // when
        starTransactionService.receiveOneStar(tester2, transaction.getId());

        // then
        assertThat(transaction.getStatus()).isEqualTo(StarTransactionStatus.RECEIVED);
        assertThat(tester2.getStarPoint()).isEqualTo(1);
    }

    @Test
    @DisplayName("이미 받은 ⭐️을 다시 받을 수 없다.")
    void receiveOneStarFail() {
        // given
        Member tester1 = createMember("tester1");
        Member tester2 = createMember("tester2");
        Follow follow = Follow.builder()
                .follower(tester1)
                .followee(tester2)
                .build();
        follow.updateStatus(FollowRequestStatus.ACCEPTED);
        followRepository.save(follow);

        StarTransaction transaction = StarTransaction.createTransaction(tester1, tester2);
        transaction.updateStatus(StarTransactionStatus.RECEIVED);
        starTransactionRepository.save(transaction);

        // when
        assertThatThrownBy(() -> starTransactionService.receiveOneStar(tester2, transaction.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(ErrorCode.ALREADY_RECEIVED.getMessage());
    }

    @Test
    @DisplayName("전달받은 모든 ⭐️을 한번에 받는다.")
    void receiveAllStars() {
        // given
        Member tester1 = createMember("tester1");
        Member tester2 = createMember("tester2");
        Member tester3 = createMember("tester3");
        Member tester4 = createMember("tester4");
        Follow follow1 = Follow.builder()
                .follower(tester2)
                .followee(tester1)
                .build();
        Follow follow2 = Follow.builder()
                .follower(tester3)
                .followee(tester1)
                .build();
        Follow follow3 = Follow.builder()
                .follower(tester4)
                .followee(tester1)
                .build();
        follow1.updateStatus(FollowRequestStatus.ACCEPTED);
        follow2.updateStatus(FollowRequestStatus.ACCEPTED);
        follow3.updateStatus(FollowRequestStatus.ACCEPTED);
        followRepository.saveAll(List.of(follow1, follow2, follow3));

        StarTransaction transaction1 = StarTransaction.createTransaction(tester2, tester1);
        StarTransaction transaction2 = StarTransaction.createTransaction(tester3, tester1);
        StarTransaction transaction3 = StarTransaction.createTransaction(tester4, tester1);
        transaction1.updateStatus(StarTransactionStatus.RECEIVED);
        starTransactionRepository.saveAll(List.of(transaction1, transaction2, transaction3));

        // when
        starTransactionService.receiveAllStars(tester1);

        // then
        assertThat(tester1.getStarPoint()).isEqualTo(2);
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