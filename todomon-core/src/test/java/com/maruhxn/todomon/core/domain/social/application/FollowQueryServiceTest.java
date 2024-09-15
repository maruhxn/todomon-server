package com.maruhxn.todomon.core.domain.social.application;

import com.maruhxn.todomon.core.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.core.domain.member.dao.TitleNameRepository;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.member.domain.TitleName;
import com.maruhxn.todomon.core.domain.social.dao.FollowRepository;
import com.maruhxn.todomon.core.domain.social.domain.Follow;
import com.maruhxn.todomon.core.domain.social.dto.response.FollowRequestItem;
import com.maruhxn.todomon.core.domain.social.dto.response.FollowerItem;
import com.maruhxn.todomon.core.domain.social.dto.response.FollowingItem;
import com.maruhxn.todomon.core.global.auth.model.Role;
import com.maruhxn.todomon.core.global.auth.model.provider.OAuth2Provider;
import com.maruhxn.todomon.core.global.common.dto.PageItem;
import com.maruhxn.todomon.core.global.common.dto.request.PagingCond;
import com.maruhxn.todomon.util.IntegrationTestSupport;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import static com.maruhxn.todomon.core.domain.social.domain.FollowRequestStatus.ACCEPTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@DisplayName("[Service] - FollowQueryService")
class FollowQueryServiceTest extends IntegrationTestSupport {

    @Autowired
    FollowQueryService followQueryService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    FollowRepository followRepository;

    @Autowired
    TitleNameRepository titleNameRepository;

    @Test
    @DisplayName("유저의 팔로잉 리스트를 조회한다")
    void followingList() {
        // given
        Member targetMember = createMember("member");
        List<Follow> acceptedFollows = createAcceptedFollowsByCnt(targetMember, 30);
        PagingCond pagingCond = new PagingCond(1);

        // when
        PageItem<FollowingItem> followingList = followQueryService.followingList(targetMember.getId(), pagingCond);
        // then
        assertThat(followingList)
                .satisfies(page -> {
                    assertThat(page.getPageNumber()).isEqualTo(1);
                    assertThat(page.getIsFirst()).isFalse();
                    assertThat(page.getIsLast()).isFalse();
                    assertThat(page.getIsEmpty()).isFalse();
                    assertThat(page.getTotalPage()).isEqualTo(3);
                    assertThat(page.getTotalElements()).isEqualTo(30L);
                });

        List<Tuple> expected = IntStream.rangeClosed(11, 20)
                .mapToObj(i -> {
                    if (i % 2 == 0) {
                        return tuple("tester" + i, "TEST", "#000000");
                    } else {
                        return tuple("tester" + i, null, null);
                    }
                })
                .sorted(Comparator.comparing(Tuple::toString).reversed())  // Tuple을 문자열로 변환하여 내림차순 정렬
                .toList();

        assertThat(followingList.getResults())
                .hasSize(10)
                .extracting("username", "title.name", "title.color")
                .containsExactlyElementsOf(expected);
    }

    @Test
    @DisplayName("나의 팔로워 리스트를 조회한다")
    void myFollowerList() {
        // given
        Member targetMember = createMember("member");
        List<Follow> pendingFollowRequests = createPendingFollowRequestsByCnt(targetMember, 30);
        pendingFollowRequests.forEach(request -> request.updateStatus(ACCEPTED));

        List<Follow> matFollowList = new ArrayList<>();
        for (int i = 1; i <= 30; i++) {
            if (i % 2 == 0) {
                Follow matFollow = Follow.builder()
                        .follower(targetMember)
                        .followee(pendingFollowRequests.get(i - 1).getFollower())
                        .build();
                matFollow.updateStatus(ACCEPTED);
                matFollowList.add(matFollow);
            }
        }

        followRepository.saveAll(pendingFollowRequests);
        followRepository.saveAll(matFollowList);

        PagingCond pagingCond = new PagingCond(1);
        // when
        PageItem<? extends FollowerItem> myFollowreList = followQueryService.followerList(targetMember.getId(), targetMember.getId(), pagingCond);

        // then
        assertThat(myFollowreList)
                .satisfies(page -> {
                    assertThat(page.getPageNumber()).isEqualTo(1);
                    assertThat(page.getIsFirst()).isFalse();
                    assertThat(page.getIsLast()).isFalse();
                    assertThat(page.getIsEmpty()).isFalse();
                    assertThat(page.getTotalPage()).isEqualTo(3);
                    assertThat(page.getTotalElements()).isEqualTo(30L);
                });

        List<Tuple> expected = IntStream.rangeClosed(11, 20)
                .mapToObj(i -> {
                    if (i % 2 == 0) {
                        return tuple("tester" + i, "TEST", "#000000", true);
                    } else {
                        return tuple("tester" + i, null, null, false);
                    }
                })
                .sorted(Comparator.comparing(Tuple::toString).reversed())  // Tuple을 문자열로 변환하여 내림차순 정렬
                .toList();

        assertThat(myFollowreList.getResults())
                .hasSize(10)
                .extracting("username", "title.name", "title.color", "isMatFollow")
                .containsExactlyElementsOf(expected);

    }

    @Test
    @DisplayName("자신이 아닌 다른 유저의 팔로워 리스트를 조회한다")
    void followerList() {
        // given
        Member targetMember = createMember("member");
        List<Follow> pendingFollowRequests = createPendingFollowRequestsByCnt(targetMember, 30);
        pendingFollowRequests.forEach(request -> request.updateStatus(ACCEPTED));

        List<Follow> matFollowList = new ArrayList<>();
        for (int i = 1; i <= 30; i++) {
            if (i % 2 == 0) {
                Follow matFollow = Follow.builder()
                        .follower(targetMember)
                        .followee(pendingFollowRequests.get(i - 1).getFollower())
                        .build();
                matFollow.updateStatus(ACCEPTED);
                matFollowList.add(matFollow);
            }
        }

        followRepository.saveAll(pendingFollowRequests);
        followRepository.saveAll(matFollowList);

        PagingCond pagingCond = new PagingCond(1);
        // when
        PageItem<? extends FollowerItem> myFollowreList = followQueryService.followerList(targetMember.getId() + 1, targetMember.getId(), pagingCond);

        // then
        assertThat(myFollowreList)
                .satisfies(page -> {
                    assertThat(page.getPageNumber()).isEqualTo(1);
                    assertThat(page.getIsFirst()).isFalse();
                    assertThat(page.getIsLast()).isFalse();
                    assertThat(page.getIsEmpty()).isFalse();
                    assertThat(page.getTotalPage()).isEqualTo(3);
                    assertThat(page.getTotalElements()).isEqualTo(30L);
                });

        List<Tuple> expected = IntStream.rangeClosed(11, 20)
                .mapToObj(i -> {
                    if (i % 2 == 0) {
                        return tuple("tester" + i, "TEST", "#000000");
                    } else {
                        return tuple("tester" + i, null, null);
                    }
                })
                .sorted(Comparator.comparing(Tuple::toString).reversed())  // Tuple을 문자열로 변환하여 내림차순 정렬
                .toList();

        assertThat(myFollowreList.getResults())
                .hasSize(10)
                .extracting("username", "title.name", "title.color")
                .containsExactlyElementsOf(expected);

    }

    @Test
    @DisplayName("유저의 아직 수락하지 않은 팔로우 요청들을 페이징 조회한다")
    void getPendingFollowRequests() {
        // given
        Member targetMember = createMember("member");
        List<Follow> pendingFollowRequests = createPendingFollowRequestsByCnt(targetMember, 30);

        PagingCond pagingCond = new PagingCond(1);

        // when
        PageItem<FollowRequestItem> pendingList = followQueryService.getPendingFollowRequests(targetMember.getId(), pagingCond);

        // then
        assertThat(pendingList)
                .satisfies(page -> {
                    assertThat(page.getPageNumber()).isEqualTo(1);
                    assertThat(page.getIsFirst()).isFalse();
                    assertThat(page.getIsLast()).isFalse();
                    assertThat(page.getIsEmpty()).isFalse();
                    assertThat(page.getTotalPage()).isEqualTo(3);
                    assertThat(page.getTotalElements()).isEqualTo(30L);
                });

        List<Tuple> expected = IntStream.rangeClosed(11, 20)
                .mapToObj(i -> {
                    if (i % 2 == 0) {
                        return tuple("tester" + i, "TEST", "#000000");
                    } else {
                        return tuple("tester" + i, null, null);
                    }
                })
                .sorted(Comparator.comparing(Tuple::toString).reversed())  // Tuple을 문자열로 변환하여 내림차순 정렬
                .toList();

        assertThat(pendingList.getResults())
                .hasSize(10)
                .extracting("username", "title.name", "title.color")
                .containsExactlyElementsOf(expected);
    }

    // 짝수번 유저의 경우 칭호도 함께 생성
    private List<Follow> createPendingFollowRequestsByCnt(Member targetMember, int cnt) {
        List<Follow> followRequests = new ArrayList<>();
        for (int i = 1; i <= cnt; i++) {
            Member tester = createMember("tester" + i);
            if (i % 2 == 0) createTitleName(tester);
            Follow follow = Follow.builder()
                    .follower(tester)
                    .followee(targetMember)
                    .build();
            followRequests.add(follow);
        }

        return followRepository.saveAll(followRequests);
    }

    private List<Follow> createAcceptedFollowsByCnt(Member targetMember, int cnt) {
        List<Follow> followRequests = new ArrayList<>();
        for (int i = 1; i <= cnt; i++) {
            Member tester = createMember("tester" + i);
            if (i % 2 == 0) createTitleName(tester);
            Follow follow = Follow.builder()
                    .follower(targetMember)
                    .followee(tester)
                    .build();
            follow.updateStatus(ACCEPTED);
            followRequests.add(follow);
        }

        return followRepository.saveAll(followRequests);
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

    private TitleName createTitleName(Member member) {
        TitleName titleName = TitleName.builder()
                .name("TEST")
                .color("#000000")
                .build();
        member.setTitleName(titleName);
        return titleNameRepository.save(titleName);
    }
}