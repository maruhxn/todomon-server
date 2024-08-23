package com.maruhxn.todomon.domain.social.dao;

import com.maruhxn.todomon.domain.member.domain.QMember;
import com.maruhxn.todomon.domain.social.domain.FollowRequestStatus;
import com.maruhxn.todomon.domain.social.domain.QFollow;
import com.maruhxn.todomon.domain.social.dto.response.*;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.maruhxn.todomon.domain.member.domain.QTitleName.titleName;
import static com.maruhxn.todomon.domain.social.domain.FollowRequestStatus.ACCEPTED;
import static com.maruhxn.todomon.domain.social.domain.FollowRequestStatus.PENDING;
import static com.maruhxn.todomon.domain.social.domain.QFollow.follow;

@Repository
@RequiredArgsConstructor
public class FollowQueryRepository {

    private final JPAQueryFactory query;

    public Page<FollowRequestItem> findPendingFollowRequestsWithPaging(Long memberId, Pageable pageable) {
        QMember sender = new QMember("sender");

        List<FollowRequestItem> results = query
                .select(
                        Projections.fields(FollowRequestItem.class,
                                follow.id,
                                sender.id.as("senderId"),
                                sender.username,
                                sender.profileImageUrl,
                                Projections.fields(AbstractMemberInfoItem.TitleNameItem.class,
                                        titleName.name,
                                        titleName.color
                                ).as("title")
                        )
                )
                .from(follow)
                .join(follow.follower, sender)
                .leftJoin(sender.titleName, titleName)
                .where(followStatusIs(PENDING), followeeIdIs(memberId))
                .orderBy(follow.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = query
                .select(follow.count())
                .from(follow)
                .where(followStatusIs(PENDING), followeeIdIs(memberId));

        results.forEach(AbstractMemberInfoItem::setTitleToNullIfIsEmpty);

        return PageableExecutionUtils.getPage(results, pageable, countQuery::fetchOne);
    }

    public Page<? extends FollowerItem> findFollowersByMemberIdWithPaging(Long memberId, Pageable pageable) {
        QMember follower = new QMember("follower");
        List<FollowerItem> results = query
                .select(
                        Projections.fields(FollowerItem.class,
                                follower.id.as("followerId"),
                                follower.username,
                                follower.profileImageUrl,
                                Projections.fields(AbstractMemberInfoItem.TitleNameItem.class,
                                        titleName.name,
                                        titleName.color
                                ).as("title")
                        )
                )
                .from(follow)
                .join(follow.follower, follower)
                .leftJoin(follower.titleName, titleName)
                .where(followeeIdIs(memberId), followStatusIs(ACCEPTED))
                .orderBy(follow.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = query
                .select(follow.count())
                .from(follow)
                .where(followeeIdIs(memberId), followStatusIs(ACCEPTED));

        results.forEach(AbstractMemberInfoItem::setTitleToNullIfIsEmpty);

        return PageableExecutionUtils.getPage(results, pageable, countQuery::fetchOne);
    }

    public Page<? extends FollowerItem> findMyFollowersWithPaging(Long memberId, Pageable pageable) {
        QMember follower = new QMember("follower");
        QFollow subFollow = new QFollow("subFollow");

        List<MyFollowerItem> results = query
                .select(
                        Projections.fields(MyFollowerItem.class,
                                follower.id.as("followerId"),
                                follower.username,
                                follower.profileImageUrl,
                                Projections.fields(AbstractMemberInfoItem.TitleNameItem.class,
                                        titleName.name,
                                        titleName.color
                                ).as("title"),
                                ExpressionUtils.as(
                                        JPAExpressions
                                                .selectOne()
                                                .from(subFollow)
                                                .where(
                                                        subFollow.followee.id.eq(follower.id)
                                                                .and(subFollow.follower.id.eq(memberId))
                                                                .and(subFollow.status.eq(ACCEPTED)))
                                                .exists(),
                                        "isMatFollow"  // 필드 이름 지정
                                )
                        )

                )
                .from(follow)
                .join(follow.follower, follower)
                .leftJoin(follower.titleName, titleName)
                .where(followeeIdIs(memberId), followStatusIs(ACCEPTED))
                .orderBy(follow.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = query
                .select(follow.count())
                .from(follow)
                .where(followeeIdIs(memberId), followStatusIs(ACCEPTED));

        results.forEach(AbstractMemberInfoItem::setTitleToNullIfIsEmpty);

        return PageableExecutionUtils.getPage(results, pageable, countQuery::fetchOne);
    }

    public Page<FollowingItem> findFollowingsByMemberIdWithPaging(Long memberId, Pageable pageable) {
        QMember followee = new QMember("followee");
        List<FollowingItem> results = query
                .select(
                        Projections.fields(FollowingItem.class,
                                followee.id.as("followeeId"),
                                followee.username,
                                followee.profileImageUrl,
                                Projections.fields(AbstractMemberInfoItem.TitleNameItem.class,
                                        titleName.name,
                                        titleName.color
                                ).as("title")
                        )
                )
                .from(follow)
                .join(follow.followee, followee)
                .leftJoin(followee.titleName, titleName)
                .where(followerIdIs(memberId), followStatusIs(ACCEPTED))
                .orderBy(follow.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = query
                .select(follow.count())
                .from(follow)
                .where(followerIdIs(memberId), followStatusIs(ACCEPTED));

        results.forEach(AbstractMemberInfoItem::setTitleToNullIfIsEmpty);

        return PageableExecutionUtils.getPage(results, pageable, countQuery::fetchOne);
    }

    private static BooleanExpression followStatusIs(FollowRequestStatus status) {
        return follow.status.eq(status);
    }

    private static BooleanExpression followeeIdIs(Long memberId) {
        return follow.followee.id.eq(memberId);
    }

    private static BooleanExpression followerIdIs(Long memberId) {
        return follow.follower.id.eq(memberId);
    }

}
