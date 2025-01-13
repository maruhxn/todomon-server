package com.maruhxn.todomon.core.domain.social.application;

import com.maruhxn.todomon.core.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.core.domain.social.dao.FollowQueryRepository;
import com.maruhxn.todomon.core.domain.social.dao.FollowRepository;
import com.maruhxn.todomon.core.domain.social.domain.Follow;
import com.maruhxn.todomon.core.domain.social.dto.response.FollowRequestItem;
import com.maruhxn.todomon.core.domain.social.dto.response.FollowerItem;
import com.maruhxn.todomon.core.domain.social.dto.response.FollowingItem;
import com.maruhxn.todomon.core.global.common.dto.PageItem;
import com.maruhxn.todomon.core.global.common.dto.request.PagingCond;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.ForbiddenException;
import com.maruhxn.todomon.core.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FollowQueryService {
    private static final int PAGE_SIZE = 10;
    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;
    private final FollowQueryRepository followQueryRepository;

    // 팔로우 요청들을 조회한다.
    public PageItem<FollowRequestItem> getPendingFollowRequests(Long memberId, PagingCond pagingCond) {
        PageRequest pageRequest = PageRequest.of(pagingCond.getPageNumber(), PAGE_SIZE);
        return PageItem.from(followQueryRepository.findPendingFollowRequestsWithPaging(memberId, pageRequest));
    }

    private void checkExistingMember(Long memberId) {
        memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_MEMBER));
    }

    public PageItem<FollowingItem> getFollowingList(Long memberId, PagingCond pagingCond) {
        this.checkExistingMember(memberId);

        PageRequest pageRequest = PageRequest.of(pagingCond.getPageNumber(), PAGE_SIZE);

        return PageItem.from(followQueryRepository.findFollowingsByMemberIdWithPaging(memberId, pageRequest));
    }

    public PageItem<? extends FollowerItem> getFollowerList(Long loginMemberId, Long memberId, PagingCond pagingCond) {
        boolean isMe = loginMemberId.equals(memberId);

        this.checkExistingMember(memberId);

        PageRequest pageRequest = PageRequest.of(pagingCond.getPageNumber(), PAGE_SIZE);

        Page<? extends FollowerItem> results;

        if (isMe) {
            results = followQueryRepository.findMyFollowersWithPaging(loginMemberId, pageRequest);
        } else {
            results = followQueryRepository.findFollowersByMemberIdWithPaging(memberId, pageRequest);
        }

        return PageItem.from(results);
    }

    public void checkIsFollow(Long follwerId, Long followeeId) {
        Follow findFollow = followRepository.findByFollower_IdAndFollowee_Id(follwerId, followeeId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_FOLLOW));

        if (!findFollow.isAccepted()) throw new ForbiddenException(ErrorCode.NOT_ACCEPTED_FOLLOW);
    }
}
