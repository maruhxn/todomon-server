package com.maruhxn.todomon.core.domain.social.domain;

import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Follow extends BaseEntity {

    @Enumerated(EnumType.STRING)
    private FollowRequestStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false, referencedColumnName = "id")
    private Member follower;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "followee_id", nullable = false, referencedColumnName = "id")
    private Member followee;

    @Builder
    public Follow(Member follower, Member followee) {
        this.status = FollowRequestStatus.PENDING;
        this.follower = follower;
        this.followee = followee;
    }

    public static Follow of(Member follower, Member followee) {
        return Follow.builder()
                .follower(follower)
                .followee(followee)
                .build();
    }

    public void updateStatus(FollowRequestStatus status) {
        this.status = status;
    }

    public boolean isPending() {
        return this.status.equals(FollowRequestStatus.PENDING);
    }

    public boolean isAccepted() {
        return this.status.equals(FollowRequestStatus.ACCEPTED);
    }
}
