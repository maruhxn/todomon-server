package com.maruhxn.todomon.domain.social.application;

import com.maruhxn.todomon.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.domain.member.domain.Member;
import com.maruhxn.todomon.domain.social.dao.FollowQueryRepository;
import com.maruhxn.todomon.domain.social.dao.FollowRepository;
import com.maruhxn.todomon.domain.social.domain.Follow;
import com.maruhxn.todomon.domain.social.dto.response.FollowRequestItem;
import com.maruhxn.todomon.domain.social.dto.response.FollowerItem;
import com.maruhxn.todomon.domain.social.dto.response.FollowingItem;
import com.maruhxn.todomon.global.common.dto.PageItem;
import com.maruhxn.todomon.global.common.dto.request.PagingCond;
import com.maruhxn.todomon.global.error.ErrorCode;
import com.maruhxn.todomon.global.error.exception.ForbiddenException;
import com.maruhxn.todomon.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.maruhxn.todomon.domain.social.domain.FollowRequestStatus.ACCEPTED;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FollowQueryService {

    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;
    private final FollowQueryRepository followQueryRepository;

    // 팔로우 요청들을 조회한다.
    public PageItem<FollowRequestItem> getPendingFollowRequests(Member member, PagingCond pagingCond) {
        PageRequest pageRequest = PageRequest.of(pagingCond.getPageNumber(), 10);
        Page<FollowRequestItem> results = followQueryRepository.findPendingFollowRequestsWithPaging(member.getId(), pageRequest);
        return PageItem.from(results);
    }

    public PageItem<FollowingItem> followingList(Long memberId, PagingCond pagingCond) {
        memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_MEMBER));

        PageRequest pageRequest = PageRequest.of(pagingCond.getPageNumber(), 10);

        Page<FollowingItem> results = followQueryRepository.findFollowingsByMemberIdWithPaging(memberId, pageRequest);
        return PageItem.from(results);
    }

    public PageItem<? extends FollowerItem> followerList(Long loginMemberId, Long memberId, PagingCond pagingCond) {
        boolean isMe = loginMemberId.equals(memberId);

        memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_MEMBER));

        PageRequest pageRequest = PageRequest.of(pagingCond.getPageNumber(), 10);

        Page<? extends FollowerItem> results;

        if (isMe) {
            results = followQueryRepository.findMyFollowersWithPaging(loginMemberId, pageRequest);
        } else {
            results = followQueryRepository.findFollowersByMemberIdWithPaging(memberId, pageRequest);
        }

        return PageItem.from(results);

    }

//    /* 맞팔로우 여부 확인 */
//    public boolean checkIsMatFollow(Long followerId, Long followeeId) {
//        Follow follow1 = followRepository.findByFollower_IdAndFollowee_Id(followerId, followeeId)
//                .orElse(null);
//
//        Follow follow2 = followRepository.findByFollower_IdAndFollowee_Id(followeeId, followerId)
//                .orElse(null);
//
//        // null 또는 ACCEPTED가 아닌 상태를 한 번에 체크
//        if (isInvalidFollow(follow1) || isInvalidFollow(follow2)) {
//            return false;
//        }
//
//        return true;
//    }

//    private boolean isInvalidFollow(Follow follow) {
//        return follow == null || follow.getStatus() != ACCEPTED;
//    }

    public void checkIsFollow(Long follwerId, Long followeeId) {
        Follow findFollow = followRepository.findByFollower_IdAndFollowee_Id(follwerId, followeeId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_FOLLOW));

        if (findFollow.getStatus() != ACCEPTED) throw new ForbiddenException(ErrorCode.NOT_ACCEPTED_FOLLOW);
    }
}
