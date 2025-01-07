package com.maruhxn.todomon.core.domain.member.dao;

import com.maruhxn.todomon.core.domain.member.dto.response.MemberSearchRes;
import com.maruhxn.todomon.core.domain.member.dto.response.ProfileRes;
import com.maruhxn.todomon.core.domain.pet.domain.QPet;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.maruhxn.todomon.core.domain.member.domain.QDiligence.diligence;
import static com.maruhxn.todomon.core.domain.member.domain.QMember.member;
import static com.maruhxn.todomon.core.domain.member.domain.QTitleName.titleName;
import static com.maruhxn.todomon.core.domain.social.domain.FollowRequestStatus.ACCEPTED;
import static com.maruhxn.todomon.core.domain.social.domain.QFollow.follow;

@Repository
@RequiredArgsConstructor
public class MemberQueryRepository {

    private final JPAQueryFactory query;

    public List<MemberSearchRes> findMemberByNameKey(String key) {
        return query
                .select(
                        Projections.fields(MemberSearchRes.class,
                                member.id.as("memberId"),
                                member.username
                        )
                )
                .from(member)
                .where(member.username.startsWith(key))
                .limit(5)
                .fetch();
    }

    public Optional<ProfileRes> getMemberProfileById(Long loginMemberId, Long memberId) {
        QPet representPet = new QPet("representPet");

        ProfileRes profileRes = query
                .select(
                        Projections.fields(ProfileRes.class,
                                member.id,
                                member.username,
                                member.email,
                                member.profileImageUrl,
                                member.isSubscribed,
                                diligence.level,
                                diligence.gauge,
                                Projections.fields(ProfileRes.TitleNameItem.class,
                                        titleName.id,
                                        titleName.name,
                                        titleName.color
                                ).as("title"),
                                Projections.fields(ProfileRes.RepresentPetItem.class,
                                        representPet.id,
                                        representPet.name,
                                        representPet.rarity,
                                        representPet.appearance,
                                        representPet.color,
                                        representPet.level
                                ).as("representPetItem"),
                                Projections.fields(ProfileRes.FollowInfoItem.class,
                                        ExpressionUtils.as(
                                                JPAExpressions.select(follow.countDistinct())
                                                        .from(follow)
                                                        .where(follow.followee.id.eq(memberId), follow.status.eq(ACCEPTED)),
                                                "followerCnt"
                                        ),
                                        ExpressionUtils.as(
                                                JPAExpressions.select(follow.countDistinct())
                                                        .from(follow)
                                                        .where(follow.follower.id.eq(memberId), follow.status.eq(ACCEPTED)),
                                                "followingCnt"
                                        ),
                                        ExpressionUtils.as(
                                                JPAExpressions
                                                        .selectOne()
                                                        .from(follow)
                                                        .where(
                                                                follow.followee.id.eq(memberId)
                                                                        .and(follow.follower.id.eq(loginMemberId))
                                                        )
                                                        .exists(),
                                                "isFollowing"
                                        ),
                                        ExpressionUtils.as(
                                                JPAExpressions
                                                        .select(follow.id)
                                                        .from(follow)
                                                        .where(follow.follower.id.eq(memberId)
                                                                .and(follow.followee.id.eq(loginMemberId))),
                                                "receivedRequestId"
                                        ),
                                        ExpressionUtils.as(
                                                JPAExpressions
                                                        .select(follow.status)
                                                        .from(follow)
                                                        .where(
                                                                follow.follower.id.eq(memberId)
                                                                        .and(follow.followee.id.eq(loginMemberId))
                                                        ),
                                                "receivedFollowStatus"
                                        ),
                                        ExpressionUtils.as(
                                                JPAExpressions
                                                        .select(follow.status)
                                                        .from(follow)
                                                        .where(
                                                                follow.followee.id.eq(memberId)
                                                                        .and(follow.follower.id.eq(loginMemberId))
                                                        ),
                                                "sentFollowStatus"
                                        )
                                ).as("followInfo")

                        )
                )
                .from(member)
                .join(member.diligence, diligence)
                .leftJoin(member.titleName, titleName)
                .leftJoin(member.representPet, representPet)
                .where(member.id.eq(memberId))
                .fetchOne();

        if (profileRes != null) {
            profileRes.setTitleNameItemToNullIfIsEmpty();
            profileRes.setRepresentPetItemToNullIfIsEmpty();
        }

        return Optional.ofNullable(profileRes);
    }
}
