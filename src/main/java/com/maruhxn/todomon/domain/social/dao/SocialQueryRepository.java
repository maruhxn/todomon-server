package com.maruhxn.todomon.domain.social.dao;

import com.maruhxn.todomon.domain.member.domain.Member;
import com.maruhxn.todomon.domain.social.domain.FollowRequestStatus;
import com.maruhxn.todomon.domain.social.dto.response.CollectedPetRankItem;
import com.maruhxn.todomon.domain.social.dto.response.DiligenceRankItem;
import com.maruhxn.todomon.domain.social.dto.response.TodoAchievementRankItem;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import static com.maruhxn.todomon.domain.member.domain.QDiligence.diligence;
import static com.maruhxn.todomon.domain.member.domain.QMember.member;
import static com.maruhxn.todomon.domain.pet.domain.QCollectedPet.collectedPet;
import static com.maruhxn.todomon.domain.social.domain.QFollow.follow;
import static com.maruhxn.todomon.domain.todo.domain.QTodoAchievementHistory.todoAchievementHistory;

@Repository
@RequiredArgsConstructor
public class SocialQueryRepository {

    private final JPAQueryFactory query;

    public List<DiligenceRankItem> findTop10MembersByDiligenceLevelAndGauge(Member currentMember) {
        return query
                .select(
                        Projections.fields(DiligenceRankItem.class,
                                member.id,
                                member.username,
                                member.profileImageUrl,
                                diligence.level
                        )
                )
                .from(member)
                .leftJoin(follow).on(follow.followee.id.eq(member.id).and(isAccepted()))
                .join(member.diligence, diligence)
                .where(
                        followerIsCurrentMember(currentMember.getId())
                                .or(isCurrentMember(currentMember.getId()))
                )
                .orderBy(diligence.level.desc(), diligence.gauge.desc(), member.createdAt.asc())
                .limit(10)
                .fetch();
    }

    public List<CollectedPetRankItem> findTop10MembersByCollectedPetCnt(Member currentMember) {
        return query.select(
                        Projections.fields(CollectedPetRankItem.class,
                                member.id,
                                member.username,
                                member.profileImageUrl,
                                collectedPet.id.count().intValue().as("petCnt"),
                                collectedPet.createdAt.max().as("lastCollectedAt")
                        )
                )
                .from(member)
                .leftJoin(follow).on(follow.followee.id.eq(member.id).and(isAccepted()))
                .leftJoin(member.collectedPets, collectedPet)
                .where(
                        followerIsCurrentMember(currentMember.getId())
                                .or(isCurrentMember(currentMember.getId()))
                )
                .groupBy(member.id, member.username, member.profileImageUrl)
                .orderBy(collectedPet.id.count().desc(), collectedPet.createdAt.max().desc(), member.createdAt.asc())
                .limit(10)
                .fetch();
    }

    public List<TodoAchievementRankItem> findTop10MembersByYesterdayAchievement(Member currentMember) {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        return query.select(
                        Projections.fields(TodoAchievementRankItem.class,
                                member.id,
                                member.username,
                                member.profileImageUrl,
                                todoAchievementHistory.cnt
                        )
                )
                .from(member)
                .leftJoin(follow).on(follow.followee.id.eq(member.id).and(isAccepted()))
                .join(member.todoAchievementHistories, todoAchievementHistory)
                .where(
                        (
                                followerIsCurrentMember(currentMember.getId())
                                        .or(isCurrentMember(currentMember.getId()))
                        )
                                .and(todoAchievementHistory.date.eq(yesterday))
                )
                .groupBy(member.id)
                .orderBy(todoAchievementHistory.cnt.sum().desc(), todoAchievementHistory.createdAt.max().asc(), member.createdAt.asc())
                .limit(10)
                .fetch();
    }

    private static BooleanExpression isAccepted() {
        return follow.status.eq(FollowRequestStatus.ACCEPTED);
    }

    public List<TodoAchievementRankItem> findTop10MembersByWeeklyAchievement(Member currentMember) {
        LocalDate startOfCurrentWeek = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate startOfLastWeek = startOfCurrentWeek.minusWeeks(1);
        LocalDate endOfLastWeek = startOfCurrentWeek.minusDays(1);

        return query.select(
                        Projections.fields(TodoAchievementRankItem.class,
                                member.id,
                                member.username,
                                member.profileImageUrl,
                                todoAchievementHistory.cnt
                        )
                )
                .from(member)
                .leftJoin(follow).on(follow.followee.id.eq(member.id).and(isAccepted()))
                .join(member.todoAchievementHistories, todoAchievementHistory)
                .where(
                        (
                                followerIsCurrentMember(currentMember.getId())
                                        .or(isCurrentMember(currentMember.getId()))
                        )
                                .and(todoAchievementHistory.date.between(startOfLastWeek, endOfLastWeek))
                )
                .groupBy(member.id)
                .orderBy(todoAchievementHistory.cnt.sum().desc(), todoAchievementHistory.createdAt.max().asc(), member.createdAt.asc())
                .limit(10)
                .fetch();
    }


    private BooleanExpression followerIsCurrentMember(Long currentMemberId) {
        return currentMemberId != null ? follow.follower.id.eq(currentMemberId) : null;
    }

    private BooleanExpression isCurrentMember(Long currentMemberId) {
        return currentMemberId != null ? member.id.eq(currentMemberId) : null;
    }
}

