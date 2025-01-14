package com.maruhxn.todomon.core.domain.social.implement;

import com.maruhxn.todomon.core.domain.social.domain.Follow;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.BadRequestException;
import com.maruhxn.todomon.core.global.error.exception.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FollowValidator {

    private final FollowReader followReader;

    public void checkIsSelfFollow(Long followerId, Long followeeId) {
        if (followerId.equals(followeeId))
            throw new BadRequestException(ErrorCode.FOLLOW_ONESELF);
    }

    public void checkIsFollow(Long followerId, Long followeeId) {
        Follow follow = followReader.findByFollowerIdAndFolloweeId(followerId, followeeId);
        if (!follow.isAccepted()) throw new ForbiddenException(ErrorCode.NOT_ACCEPTED_FOLLOW);
    }
}
