package com.maruhxn.todomon.core.domain.social.implement;

import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.social.dao.FollowRepository;
import com.maruhxn.todomon.core.domain.social.domain.Follow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.maruhxn.todomon.core.domain.social.domain.FollowRequestStatus.ACCEPTED;

@Component
@RequiredArgsConstructor
public class FollowManager {

    private final FollowRepository followRepository;

    public void sendFollowRequest(Member follower, Member followee) {
        followRepository.save(Follow.of(follower, followee));
    }

    public void matFollow(Member follower, Member followee) {
        Follow matFollow = Follow.of(follower, followee);
        matFollow.updateStatus(ACCEPTED);
        followRepository.save(matFollow);
    }

    public void remove(Follow follow) {
        followRepository.delete(follow);
    }
}
