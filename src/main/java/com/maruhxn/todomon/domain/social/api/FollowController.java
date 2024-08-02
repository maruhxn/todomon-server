package com.maruhxn.todomon.domain.social.api;

import com.maruhxn.todomon.domain.social.application.FollowQueryService;
import com.maruhxn.todomon.domain.social.application.FollowService;
import com.maruhxn.todomon.domain.social.dto.response.FollowItem;
import com.maruhxn.todomon.global.auth.model.TodomonOAuth2User;
import com.maruhxn.todomon.global.common.dto.response.DataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/social/follow")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;
    private final FollowQueryService followQueryService;

    @PostMapping("/{memberId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void follow(
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User,
            @PathVariable("memberId") Long memberId
    ) {
        followService.sendFollowRequest(todomonOAuth2User.getMember(), memberId);
    }

    @PatchMapping("/accept/{followId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@authChecker.isMyFollowOrAdmin(#followId)")
    public void respondFollow(
            @PathVariable Long followId,
            @RequestParam(required = true) Boolean isAccepted
    ) {
        followService.respondToFollowRequest(followId, isAccepted);
    }

    @DeleteMapping("/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unfollow(
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User,
            @PathVariable("memberId") Long memberId
    ) {
        followService.unfollow(todomonOAuth2User.getId(), memberId);
    }

    @GetMapping("/{memberId}/followers")
    public DataResponse<List<FollowItem>> getFollowers(
            @PathVariable("memberId") Long memberId
    ) {
        return DataResponse.of("팔로워 리스트 조회 성공", followQueryService.followerList(memberId));
    }

    @GetMapping("/{memberId}/followings")
    public DataResponse<List<FollowItem>> getFollowings(
            @PathVariable("memberId") Long memberId
    ) {
        return DataResponse.of("팔로잉 리스트 조회 성공", followQueryService.followingList(memberId));
    }
}
