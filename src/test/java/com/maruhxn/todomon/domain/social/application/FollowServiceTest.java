package com.maruhxn.todomon.domain.social.application;

import com.maruhxn.todomon.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.domain.member.domain.Member;
import com.maruhxn.todomon.domain.social.dao.FollowRepository;
import com.maruhxn.todomon.domain.social.domain.Follow;
import com.maruhxn.todomon.global.auth.model.Role;
import com.maruhxn.todomon.global.auth.model.provider.OAuth2Provider;
import com.maruhxn.todomon.util.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.maruhxn.todomon.domain.social.domain.FollowRequestStatus.*;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("[Service] - FollowService")
class FollowServiceTest extends IntegrationTestSupport {

    @Autowired
    FollowService followService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    FollowRepository followRepository;

    @Test
    @DisplayName("팔로우 요청을 보낸다")
    void sendFollowRequest() {
        // given
        Member tester1 = createMember("tester1");
        Member tester2 = createMember("tester2");

        // when
        followService.sendFollowRequest(tester1, tester2.getId());

        // then
        assertThat(followRepository.findAll())
                .hasSize(1)
                .first()
                .extracting("follower", "followee", "status")
                .containsExactly(tester1, tester2, PENDING);
    }

    @Test
    @DisplayName("팔로우 요청을 수락한다")
    void acceptFollow() {
        // given
        Member tester1 = createMember("tester1");
        Member tester2 = createMember("tester2");
        Follow follow = Follow.builder()
                .follower(tester1)
                .followee(tester2)
                .build();
        followRepository.save(follow);

        // when
        followService.respondToFollowRequest(follow.getId(), true);

        // then
        assertThat(followRepository.findAll())
                .hasSize(1)
                .first()
                .extracting("follower", "followee", "status")
                .containsExactly(tester1, tester2, ACCEPTED);
    }

    @Test
    @DisplayName("팔로우 요청을 거절한다")
    void rejectFollow() {
        // given
        Member tester1 = createMember("tester1");
        Member tester2 = createMember("tester2");
        Follow follow = Follow.builder()
                .follower(tester1)
                .followee(tester2)
                .build();
        followRepository.save(follow);

        // when
        followService.respondToFollowRequest(follow.getId(), false);

        // then
        assertThat(followRepository.findAll())
                .hasSize(1)
                .first()
                .extracting("follower", "followee", "status")
                .containsExactly(tester1, tester2, REJECTED);
    }

    @Test
    @DisplayName("언팔로우")
    void unfollow() {
        // given
        Member tester1 = createMember("tester1");
        Member tester2 = createMember("tester2");
        Follow follow = Follow.builder()
                .follower(tester1)
                .followee(tester2)
                .build();
        follow.updateStatus(ACCEPTED);
        followRepository.save(follow);

        // when
        followService.unfollow(tester1, tester2.getId());

        // then
        assertThat(followRepository.findById(follow.getId())).isEmpty();
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