package com.maruhxn.todomon.core.domain.social.application;

import com.maruhxn.todomon.core.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.social.dao.FollowRepository;
import com.maruhxn.todomon.core.domain.social.domain.Follow;
import com.maruhxn.todomon.core.global.auth.checker.IsMyFollowOrAdmin;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.BadRequestException;
import com.maruhxn.todomon.core.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.maruhxn.todomon.core.domain.social.domain.FollowRequestStatus.ACCEPTED;
import static com.maruhxn.todomon.core.domain.social.domain.FollowRequestStatus.REJECTED;

@Service
@Transactional
@RequiredArgsConstructor
public class FollowService {

    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;

    public void sendFollowRequestOrMatFollow(Long followerId, Long followeeId) {
        // 자기 자신은 팔로우 안됨
        if (followerId.equals(followeeId))
            throw new BadRequestException(ErrorCode.FOLLOW_ONESELF);

        Member followee = memberRepository.findById(followeeId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_MEMBER, "팔로위 정보가 존재하지 않습니다."));

        Member follower = memberRepository.findById(followerId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_MEMBER));

        followRepository.findByFollower_IdAndFollowee_Id(followeeId, followerId)
                .ifPresentOrElse(
                        receivedFollow -> this.matFollow(receivedFollow, follower, followee), // 이미 받은 팔로우가 있다면 맞팔로우
                        () -> this.sendFollowRequest(follower, followee)  // 받은 팔로우가 없다면 팔로우 요청 보내기
                );
    }

    private void sendFollowRequest(Member follower, Member followee) {
        followRepository.save(Follow.of(follower, followee));
    }

    private void matFollow(Follow receivedFollow, Member follower, Member followee) {
        if (receivedFollow.isPending()) receivedFollow.updateStatus(ACCEPTED);

        Follow matFollow = Follow.of(follower, followee);
        matFollow.updateStatus(ACCEPTED);
        followRepository.save(matFollow);
    }

    // 팔로우 요청에 대해 응답한다.
    @IsMyFollowOrAdmin
    public void respondToFollowRequest(Long followId, boolean isAccepted) {
        Follow follow = followRepository.findById(followId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_FOLLOW));

        follow.updateStatus(isAccepted ? ACCEPTED : REJECTED);
    }

    // 팔로우를 취소한다.
    public void unfollow(Long memberId, Long followeeId) {
        this.findAndDeleteFollow(memberId, followeeId);
    }

    // 나를 팔로우하고 있는 팔로워를 삭제한다.
    public void removeFollower(Long memberId, Long followerId) {
        this.findAndDeleteFollow(followerId, memberId);
    }

    // unfollow와 removeFollower는 대상만 반대고 로직은 동일함.
    private void findAndDeleteFollow(Long memberId, Long followeeId) {
        if (memberId.equals(followeeId))
            throw new BadRequestException(ErrorCode.FOLLOW_ONESELF);

        memberRepository.findById(followeeId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_MEMBER, "대상 정보가 존재하지 않습니다."));

        Follow findFollow = followRepository.findByFollower_IdAndFollowee_Id(memberId, followeeId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_FOLLOW));

        followRepository.delete(findFollow);
    }
}
