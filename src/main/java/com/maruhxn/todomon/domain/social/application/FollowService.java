package com.maruhxn.todomon.domain.social.application;

import com.maruhxn.todomon.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.domain.member.domain.Member;
import com.maruhxn.todomon.domain.social.dao.FollowRepository;
import com.maruhxn.todomon.domain.social.domain.Follow;
import com.maruhxn.todomon.global.error.ErrorCode;
import com.maruhxn.todomon.global.error.exception.BadRequestException;
import com.maruhxn.todomon.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.maruhxn.todomon.domain.social.domain.FollowRequestStatus.*;

@Service
@Transactional
@RequiredArgsConstructor
public class FollowService {

    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;

    // 팔로우 요청을 보낸다.
    public void sendFollowRequestOrMatFollow(Member follower, Long followeeId) {
        // 자기 자신은 팔로우 안됨
        if (follower.getId() == followeeId) throw new BadRequestException(ErrorCode.BAD_REQUEST);

        Member followee = memberRepository.findById(followeeId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_MEMBER, "팔로위 정보가 존재하지 않습니다."));

        // 이미 받은 팔로우가 있다면, 맞팔로우 로직으로 전환
        Optional<Follow> receivedFollowRequest =
                followRepository.findByFollower_IdAndFollowee_Id(followeeId, follower.getId());

        if (receivedFollowRequest.isPresent()) {
            Follow receivedFollow = receivedFollowRequest.get();
            if (receivedFollow.getStatus().equals(PENDING)) {
                receivedFollow.updateStatus(ACCEPTED);
            }
            Follow matFollow = Follow.builder()
                    .follower(follower)
                    .followee(followee)
                    .build();
            matFollow.updateStatus(ACCEPTED);
            followRepository.save(matFollow);
        } else {
            followRepository.findByFollower_IdAndFollowee_Id(follower.getId(), followeeId)
                    .ifPresent(f -> {
                        throw new BadRequestException(ErrorCode.BAD_REQUEST, "이미 팔로우 요청을 보냈습니다.");
                    });

            Follow follow = Follow.builder()
                    .follower(follower)
                    .followee(followee)
                    .build();

            followRepository.save(follow);
        }
    }

    // 팔로우 요청에 대해 응답한다.
    public void respondToFollowRequest(Long followId, boolean isAccepted) {
        Follow follow = followRepository.findById(followId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_FOLLOW));

        follow.updateStatus(isAccepted ? ACCEPTED : REJECTED);
        followRepository.save(follow);
    }

    // 팔로우를 취소한다.
    public void unfollow(Long memberId, Long followeeId) {
        if (memberId.equals(followeeId))
            throw new BadRequestException(ErrorCode.BAD_REQUEST, "자기 자신에 대한 요청은 할 수 없습니다.");

        memberRepository.findById(followeeId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_MEMBER, "팔로우 대상 정보가 존재하지 않습니다."));

        Follow findFollow = followRepository.findByFollower_IdAndFollowee_Id(memberId, followeeId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_FOLLOW));

        followRepository.delete(findFollow);
    }

    // 나를 팔로우하고 있는 팔로워를 삭제한다.
    public void removeFollower(Long memberId, Long followerId) {
        if (memberId.equals(followerId))
            throw new BadRequestException(ErrorCode.BAD_REQUEST, "자기 자신에 대한 요청은 할 수 없습니다.");

        memberRepository.findById(followerId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_MEMBER, "팔로워 정보가 존재하지 않습니다."));

        Follow findFollow = followRepository.findByFollower_IdAndFollowee_Id(followerId, memberId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_FOLLOW));

        followRepository.delete(findFollow);
    }

//    public void matFollow(Member member, Long followerId) {
//        // 팔로워가 존재하는지 확인
//        Member follower = memberRepository.findById(followerId)
//                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_MEMBER, "팔로워의 정보가 존재하지 않습니다."));
//
//        // 팔로워가 나를 팔로우하고 있는지 확인 (Follow가 있고, ACCEPT)
//        followQueryService.checkIsFollow(followerId, member.getId());
//
//        // 팔로우 요청을 보내지 않고, 바로 팔로우 엔터티 생성
//        Follow matFollow = Follow.builder()
//                .follower(member)
//                .followee(follower)
//                .build();
//        matFollow.updateStatus(ACCEPTED);
//
//        followRepository.save(matFollow);
//    }
}
