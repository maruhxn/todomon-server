package com.maruhxn.todomon.domain.social.application;

import com.maruhxn.todomon.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.domain.member.domain.Member;
import com.maruhxn.todomon.domain.social.dao.FollowRepository;
import com.maruhxn.todomon.domain.social.domain.Follow;
import com.maruhxn.todomon.domain.social.dto.response.FollowItem;
import com.maruhxn.todomon.global.error.ErrorCode;
import com.maruhxn.todomon.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.maruhxn.todomon.domain.social.domain.FollowRequestStatus.ACCEPTED;
import static com.maruhxn.todomon.domain.social.domain.FollowRequestStatus.PENDING;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FollowQueryService {

    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;

    // 팔로우 요청들을 조회한다.
    public List<FollowItem> getPendingFollowRequests(Member member) {
        List<Follow> pendingRequests = followRepository.findByFolloweeAndStatusOrderByCreatedAtDesc(member, PENDING);
        List<Member> pendingFollowerList = pendingRequests.stream()
                .map(Follow::getFollower)
                .toList();
        return pendingFollowerList.stream().map(FollowItem::of).toList();
    }

    public List<FollowItem> followingList(Long memberId) {
        Member findMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_MEMBER));

        List<Follow> followings = followRepository.findByFollowerAndStatusOrderByCreatedAtDesc(findMember, ACCEPTED);

        List<Member> followingList = followings.stream()
                .map(Follow::getFollowee)
                .toList();

        return followingList.stream().map(FollowItem::of).toList();
    }

    public List<FollowItem> followerList(Long memberId) {
        Member findMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_MEMBER));

        List<Follow> followers = followRepository.findByFolloweeAndStatusOrderByCreatedAtDesc(findMember, ACCEPTED);

        List<Member> followerList = followers.stream()
                .map(Follow::getFollower)
                .toList();

        return followerList.stream().map(FollowItem::of).toList();
    }
}
