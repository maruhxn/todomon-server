package com.maruhxn.todomon.core.domain.social.implement;

import com.maruhxn.todomon.core.domain.social.dao.FollowQueryRepository;
import com.maruhxn.todomon.core.domain.social.dao.FollowRepository;
import com.maruhxn.todomon.core.domain.social.domain.Follow;
import com.maruhxn.todomon.core.domain.social.dto.response.FollowRequestItem;
import com.maruhxn.todomon.core.domain.social.dto.response.FollowerItem;
import com.maruhxn.todomon.core.domain.social.dto.response.FollowingItem;
import com.maruhxn.todomon.core.global.common.dto.request.PagingCond;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.ForbiddenException;
import com.maruhxn.todomon.core.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FollowReader {
    private static final int PAGE_SIZE = 10;

    private final FollowRepository followRepository;
    private final FollowQueryRepository followQueryRepository;

    public Page<FollowRequestItem> findPendingRequests(Long memberId, PagingCond pagingCond) {
        PageRequest pageRequest = PageRequest.of(pagingCond.getPageNumber(), PAGE_SIZE);
        return followQueryRepository.findPendingFollowRequestsWithPaging(memberId, pageRequest);
    }

    public Page<FollowingItem> findFollowings(Long memberId, PagingCond pagingCond) {
        PageRequest pageRequest = PageRequest.of(pagingCond.getPageNumber(), PAGE_SIZE);
        return followQueryRepository.findFollowingsByMemberIdWithPaging(memberId, pageRequest);
    }

    public Page<? extends FollowerItem> findMyFollowers(Long myId, PagingCond pagingCond) {
        PageRequest pageRequest = PageRequest.of(pagingCond.getPageNumber(), PAGE_SIZE);
        return followQueryRepository.findMyFollowersWithPaging(myId, pageRequest);
    }

    public Page<? extends FollowerItem> findFollowersByMemberId(Long memberId, PagingCond pagingCond) {
        PageRequest pageRequest = PageRequest.of(pagingCond.getPageNumber(), PAGE_SIZE);
        return followQueryRepository.findFollowersByMemberIdWithPaging(memberId, pageRequest);
    }

    public Follow findByFollowerIdAndFolloweeId(Long followerId, Long followeeId) {
        return followRepository.findByFollower_IdAndFollowee_Id(followerId, followeeId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_FOLLOW));
    }

    public Optional<Follow> findOptionalByFollowerIdAndFolloweeId(Long followerId, Long followeeId) {
        return followRepository.findByFollower_IdAndFollowee_Id(followerId, followeeId);
    }

    public Follow findById(Long id) {
        return followRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_FOLLOW));
    }


}
