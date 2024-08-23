package com.maruhxn.todomon.domain.social.dao;

import com.maruhxn.todomon.domain.member.domain.Member;
import com.maruhxn.todomon.domain.social.domain.Follow;
import com.maruhxn.todomon.domain.social.domain.FollowRequestStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    Optional<Follow> findByFollower_IdAndFollowee_Id(Long followerId, Long followeeId);

    @EntityGraph(attributePaths = {"followee.titleName"})
    List<Follow> findByFollowerAndStatusOrderByCreatedAtDesc(Member follower, FollowRequestStatus followRequestStatus); // follower가 전달값과 일치하는 Follow 모두 찾기

    @EntityGraph(attributePaths = {"follower.titleName"})
    List<Follow> findByFolloweeAndStatusOrderByCreatedAtDesc(Member followee, FollowRequestStatus followRequestStatus); // followee가 전달값과 일치하는 Follow 모두 찾기

    boolean existsByFollowerAndFolloweeAndStatus(Member follower, Member followee, FollowRequestStatus followRequestStatus);

}
