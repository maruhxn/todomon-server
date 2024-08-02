package com.maruhxn.todomon.domain.social.application;

import com.maruhxn.todomon.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.domain.member.domain.Member;
import com.maruhxn.todomon.domain.social.dao.FollowRepository;
import com.maruhxn.todomon.domain.social.domain.Follow;
import com.maruhxn.todomon.global.error.ErrorCode;
import com.maruhxn.todomon.global.error.exception.BadRequestException;
import com.maruhxn.todomon.global.error.exception.ForbiddenException;
import com.maruhxn.todomon.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.maruhxn.todomon.domain.social.domain.FollowRequestStatus.ACCEPTED;
import static com.maruhxn.todomon.domain.social.domain.FollowRequestStatus.REJECTED;

@Service
@Transactional
@RequiredArgsConstructor
public class FollowService {

    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;

    // 팔로우 요청을 보낸다.
    public void sendFollowRequest(Member follower, Long followeeId) {
        // 자기 자신은 팔로우 안됨
        if (follower.getId() == followeeId) throw new BadRequestException(ErrorCode.BAD_REQUEST);

        // 팔로우 정보가 이미 존재하는지 확인
        followRepository.findByFollower_IdAndFollowee_Id(follower.getId(), followeeId)
                .ifPresent(f -> {
                    throw new BadRequestException(ErrorCode.BAD_REQUEST);
                });

        Member followee = memberRepository.findById(followeeId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_MEMBER, "팔로위 정보가 존재하지 않습니다."));

        Follow follow = Follow.builder()
                .follower(follower)
                .followee(followee)
                .build();

        follower.getFollowings().add(follow);
        followRepository.save(follow);
    }

    // 팔로우 요청에 대해 응답한다.
    public void respondToFollowRequest(Long followId, boolean isAccepted) {
        Follow follow = followRepository.findById(followId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_FOLLOW));

        follow.updateStatus(isAccepted ? ACCEPTED : REJECTED);
        followRepository.save(follow);
    }

    // 팔로우를 취소한다.
    public void unfollow(Long memberId, Long targetId) {
        if (memberId == targetId) throw new BadRequestException(ErrorCode.BAD_REQUEST);

        memberRepository.findById(targetId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_MEMBER, "대상 유저의 정보가 존재하지 않습니다."));

        Follow findFollow = followRepository.findByFollower_IdAndFollowee_Id(memberId, targetId)
                .orElseGet(() -> followRepository.findByFollower_IdAndFollowee_Id(targetId, memberId)
                        .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_FOLLOW)));

        followRepository.delete(findFollow);
    }

    public void checkIsFollow(Long follwerId, Long followeeId) {
        Follow findFollow = followRepository.findByFollower_IdAndFollowee_Id(follwerId, followeeId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_FOLLOW));

        if (findFollow.getStatus() != ACCEPTED) throw new ForbiddenException(ErrorCode.NOT_ACCEPTED_FOLLOW);
    }
}
