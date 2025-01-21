package com.maruhxn.todomon.core.domain.social.application;

import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.member.implement.MemberReader;
import com.maruhxn.todomon.core.domain.social.domain.Follow;
import com.maruhxn.todomon.core.domain.social.implement.FollowManager;
import com.maruhxn.todomon.core.domain.social.implement.FollowReader;
import com.maruhxn.todomon.core.domain.social.implement.FollowValidator;
import com.maruhxn.todomon.core.global.auth.checker.IsMyFollowOrAdmin;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.ExistingResourceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.maruhxn.todomon.core.domain.social.domain.FollowRequestStatus.ACCEPTED;
import static com.maruhxn.todomon.core.domain.social.domain.FollowRequestStatus.REJECTED;

@Service
@Transactional
@RequiredArgsConstructor
public class FollowService {

    private final MemberReader memberReader;
    private final FollowReader followReader;
    private final FollowManager followManager;
    private final FollowValidator followValidator;

    public void sendFollowRequest(Long followerId, Long followeeId) {
        // 자기 자신은 팔로우 안됨
        followValidator.checkIsSelfFollow(followerId, followeeId);

        Member loginMember = memberReader.findById(followerId);
        Member followee = memberReader.findById(followeeId, "팔로우 대상 정보가 존재하지 않습니다.");

        followReader.findOptionalByFollowerIdAndFolloweeId(followerId, followeeId)
                .ifPresent(r -> {
                    throw new ExistingResourceException(ErrorCode.ALREADY_SENT_REQUEST);
                });

        followReader.findOptionalByFollowerIdAndFolloweeId(followeeId, followerId)
                .ifPresentOrElse(
                        receivedFollow -> {
                            if (receivedFollow.isPending()) receivedFollow.updateStatus(ACCEPTED);
                            followManager.matFollow(loginMember, followee);
                        }, // 이미 받은 팔로우가 있다면 맞팔로우
                        () -> followManager.sendFollowRequest(loginMember, followee)  // 받은 팔로우가 없다면 팔로우 요청 보내기
                );
    }

    // 팔로우 요청에 대해 응답한다.
    @IsMyFollowOrAdmin
    public void respondToFollowRequest(Long followId, boolean isAccepted) {
        Follow follow = followReader.findById(followId);
        follow.updateStatus(isAccepted ? ACCEPTED : REJECTED);
    }

    // unfollow와 removeFollower는 대상만 반대고 로직은 동일함.
    private void findAndDeleteFollow(Long memberId, Long followeeId) {
        followValidator.checkIsSelfFollow(memberId, followeeId);
        memberReader.findById(followeeId, "팔로우 대상 정보가 존재하지 않습니다.");
        Follow follow = followReader.findByFollowerIdAndFolloweeId(memberId, followeeId);
        followManager.remove(follow);
    }

    // 팔로우를 취소한다.
    public void unfollow(Long memberId, Long followeeId) {
        this.findAndDeleteFollow(memberId, followeeId);
    }

    // 나를 팔로우하고 있는 팔로워를 삭제한다.
    public void removeFollower(Long memberId, Long followerId) {
        this.findAndDeleteFollow(followerId, memberId);
    }

}
