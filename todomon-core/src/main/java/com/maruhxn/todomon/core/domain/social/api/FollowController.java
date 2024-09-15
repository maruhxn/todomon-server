package com.maruhxn.todomon.core.domain.social.api;

import com.maruhxn.todomon.core.domain.social.application.FollowQueryService;
import com.maruhxn.todomon.core.domain.social.application.FollowService;
import com.maruhxn.todomon.core.domain.social.dto.response.FollowRequestItem;
import com.maruhxn.todomon.core.domain.social.dto.response.FollowerItem;
import com.maruhxn.todomon.core.domain.social.dto.response.FollowingItem;
import com.maruhxn.todomon.core.global.auth.model.TodomonOAuth2User;
import com.maruhxn.todomon.core.global.common.dto.PageItem;
import com.maruhxn.todomon.core.global.common.dto.request.PagingCond;
import com.maruhxn.todomon.core.global.common.dto.response.DataResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/social/follows")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;
    private final FollowQueryService followQueryService;

    @GetMapping("/requests/pending")
    public DataResponse<PageItem<FollowRequestItem>> getPendingFollowRequests(
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User,
            @ModelAttribute @Valid PagingCond pagingCond
    ) {
        PageItem<FollowRequestItem> results = followQueryService.getPendingFollowRequests(todomonOAuth2User.getId(), pagingCond);
        return DataResponse.of("팔로우 요청 조회 성공", results);
    }

    @PostMapping("/{memberId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void followRequest(
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User,
            @PathVariable("memberId") Long memberId
    ) {
        followService.sendFollowRequestOrMatFollow(todomonOAuth2User.getId(), memberId);
    }

    @PatchMapping("/requests/{followId}/respond")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void respondToFollowRequest(
            @PathVariable Long followId,
            @RequestParam(required = true) Boolean isAccepted
    ) {
        followService.respondToFollowRequest(followId, isAccepted);
    }

    @DeleteMapping("/{followerId}/remove")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFollower(
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User,
            @PathVariable("followerId") Long followerId
    ) {
        followService.removeFollower(todomonOAuth2User.getId(), followerId);
    }

    @DeleteMapping("/{followeeId}/unfollow")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unfollow(
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User,
            @PathVariable("followeeId") Long followeeId
    ) {
        followService.unfollow(todomonOAuth2User.getId(), followeeId);
    }

    @GetMapping("/{memberId}/followers")
    public DataResponse<PageItem<? extends FollowerItem>> getFollowers(
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User,
            @PathVariable("memberId") Long memberId,
            @ModelAttribute @Valid PagingCond pagingCond
    ) {
        return DataResponse.of("팔로워 리스트 조회 성공", followQueryService.followerList(todomonOAuth2User.getId(), memberId, pagingCond));
    }

    @GetMapping("/{memberId}/followings")
    public DataResponse<PageItem<FollowingItem>> getFollowings(
            @PathVariable("memberId") Long memberId,
            @ModelAttribute @Valid PagingCond pagingCond
    ) {
        return DataResponse.of("팔로잉 리스트 조회 성공", followQueryService.followingList(memberId, pagingCond));
    }
}
