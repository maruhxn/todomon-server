package com.maruhxn.todomon.domain.social.application;

import com.maruhxn.todomon.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.domain.member.domain.Member;
import com.maruhxn.todomon.domain.social.dao.FollowRepository;
import com.maruhxn.todomon.domain.social.domain.Follow;
import com.maruhxn.todomon.domain.social.dto.response.FollowItem;
import com.maruhxn.todomon.global.auth.model.Role;
import com.maruhxn.todomon.global.auth.model.provider.OAuth2Provider;
import com.maruhxn.todomon.util.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.maruhxn.todomon.domain.social.domain.FollowRequestStatus.ACCEPTED;
import static com.maruhxn.todomon.domain.social.domain.FollowRequestStatus.REJECTED;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("[Service] - FollowQueryService")
class FollowQueryServiceTest extends IntegrationTestSupport {

    @Autowired
    FollowQueryService followQueryService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    FollowRepository followRepository;

    @Test
    @DisplayName("유저의 팔로잉 리스트를 조회한다")
    void followingList() {
        // given
        Member tester1 = createMember("tester1");
        Member tester2 = createMember("tester2");
        Member tester3 = createMember("tester3");
        Member tester4 = createMember("tester4");

        Follow follow1 = Follow.builder()
                .follower(tester1)
                .followee(tester2)
                .build();
        Follow follow2 = Follow.builder()
                .follower(tester1)
                .followee(tester3)
                .build();

        Follow follow3 = Follow.builder()
                .follower(tester1)
                .followee(tester4)
                .build();

        follow1.updateStatus(ACCEPTED);
        follow2.updateStatus(REJECTED);
        follow3.updateStatus(ACCEPTED);
        followRepository.saveAll(List.of(follow1, follow2, follow3));
        // when
        List<FollowItem> followingList = followQueryService.followingList(tester1.getId());

        // then
        assertThat(followingList)
                .hasSize(2)
                .first()
                .extracting("id", "username", "profileImageUrl")
                .containsExactly(tester4.getId(), tester4.getUsername(), tester4.getProfileImageUrl());
    }

    @Test
    @DisplayName("유저의 팔로워 리스트를 조회한다")
    void followerList() {
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

        follow1.updateStatus(ACCEPTED);
        follow2.updateStatus(REJECTED);
        follow3.updateStatus(ACCEPTED);
        followRepository.saveAll(List.of(follow1, follow2, follow3));
        // when
        List<FollowItem> followerList = followQueryService.followerList(tester1.getId());

        // then
        assertThat(followerList)
                .hasSize(2)
                .first()
                .extracting("id", "username", "profileImageUrl")
                .containsExactly(tester4.getId(), tester4.getUsername(), tester4.getProfileImageUrl());
    }

    @Test
    @DisplayName("유저의 아직 수락하지 않은 팔로우 요청들을 조회한다")
    void getPendingFollowRequests() {
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

        follow1.updateStatus(ACCEPTED);
        followRepository.saveAll(List.of(follow1, follow2, follow3));
        // when
        List<FollowItem> pendingList = followQueryService.getPendingFollowRequests(tester1);

        // then
        assertThat(pendingList)
                .hasSize(2)
                .first()
                .extracting("id", "username", "profileImageUrl")
                .containsExactly(tester4.getId(), tester4.getUsername(), tester4.getProfileImageUrl());
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