package com.maruhxn.todomon.core.domain.social.dao;

import com.maruhxn.todomon.core.domain.social.dto.response.AbstractMemberInfoItem;
import com.maruhxn.todomon.core.domain.social.dto.response.CollectedPetRankItem;
import com.maruhxn.todomon.core.domain.social.dto.response.DiligenceRankItem;
import com.maruhxn.todomon.core.domain.social.dto.response.TodoAchievementRankItem;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import static com.maruhxn.todomon.core.domain.member.domain.QDiligence.diligence;
import static com.maruhxn.todomon.core.domain.member.domain.QMember.member;
import static com.maruhxn.todomon.core.domain.member.domain.QTitleName.titleName;
import static com.maruhxn.todomon.core.domain.pet.domain.QCollectedPet.collectedPet;
import static com.maruhxn.todomon.core.domain.todo.domain.QTodoAchievementHistory.todoAchievementHistory;

@Repository
@RequiredArgsConstructor
public class OverallRankQueryRepository {

    private final JPAQueryFactory query;

    public List<DiligenceRankItem> findTop10MembersByDiligenceLevelAndGauge() {
        List<DiligenceRankItem> results = query.select(
                        Projections.fields(DiligenceRankItem.class,
                                member.id.as("memberId"),
                                member.username,
                                member.profileImageUrl,
                                diligence.level,
                                Projections.fields(AbstractMemberInfoItem.TitleNameItem.class,
                                        titleName.name,
                                        titleName.color
                                ).as("title")
                        )
                )
                .from(member)
                .join(member.diligence, diligence)
                .leftJoin(member.titleName, titleName)
                .orderBy(diligence.level.desc(), diligence.gauge.desc(), member.createdAt.asc())
                .limit(10)
                .fetch();

        results.forEach(AbstractMemberInfoItem::setTitleToNullIfIsEmpty);

        return results;
    }

    public List<CollectedPetRankItem> findTop10MembersByCollectedPetCnt() {
        List<CollectedPetRankItem> results = query.select(
                        Projections.fields(CollectedPetRankItem.class,
                                member.id.as("memberId"),
                                member.username,
                                member.profileImageUrl,
                                collectedPet.id.count().intValue().as("petCnt"),
                                collectedPet.createdAt.max().as("lastCollectedAt"),
                                Projections.fields(AbstractMemberInfoItem.TitleNameItem.class,
                                        titleName.name,
                                        titleName.color
                                ).as("title")
                        )
                )
                .from(member)
                .leftJoin(member.collectedPets, collectedPet)
                .leftJoin(member.titleName, titleName)
                .groupBy(member.id, member.username, member.profileImageUrl)
                .orderBy(collectedPet.id.count().desc(), collectedPet.createdAt.max().desc(), member.createdAt.asc())
                .limit(10)
                .fetch();

        results.forEach(AbstractMemberInfoItem::setTitleToNullIfIsEmpty);

        return results;
    }

    public List<TodoAchievementRankItem> findTop10MembersByYesterdayAchievement() {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        List<TodoAchievementRankItem> results = query.select(
                        Projections.fields(TodoAchievementRankItem.class,
                                member.id.as("memberId"),
                                member.username,
                                member.profileImageUrl,
                                todoAchievementHistory.cnt,
                                Projections.fields(AbstractMemberInfoItem.TitleNameItem.class,
                                        titleName.name,
                                        titleName.color
                                ).as("title")
                        )
                )
                .from(todoAchievementHistory)
                .join(todoAchievementHistory.member, member)
                .leftJoin(member.titleName, titleName)
                .where(todoAchievementHistory.date.eq(yesterday))
                .groupBy(member.id)
                .orderBy(todoAchievementHistory.cnt.sum().desc(), todoAchievementHistory.createdAt.max().asc(), member.createdAt.asc())
                .limit(10)
                .fetch();

        results.forEach(AbstractMemberInfoItem::setTitleToNullIfIsEmpty);

        return results;
    }

    public List<TodoAchievementRankItem> findTop10MembersByWeeklyAchievement() {
        LocalDate startOfCurrentWeek = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate startOfLastWeek = startOfCurrentWeek.minusWeeks(1);
        LocalDate endOfLastWeek = startOfCurrentWeek.minusDays(1);

        List<TodoAchievementRankItem> results = query.select(
                        Projections.fields(TodoAchievementRankItem.class,
                                member.id.as("memberId"),
                                member.username,
                                member.profileImageUrl,
                                todoAchievementHistory.cnt,
                                Projections.fields(AbstractMemberInfoItem.TitleNameItem.class,
                                        titleName.name,
                                        titleName.color
                                ).as("title")
                        )
                )
                .from(todoAchievementHistory)
                .join(todoAchievementHistory.member, member)
                .leftJoin(member.titleName, titleName)
                .where(todoAchievementHistory.date.between(startOfLastWeek, endOfLastWeek))
                .groupBy(member.id)
                .orderBy(todoAchievementHistory.cnt.sum().desc(), todoAchievementHistory.createdAt.max().asc(), member.createdAt.asc())
                .limit(10)
                .fetch();

        results.forEach(AbstractMemberInfoItem::setTitleToNullIfIsEmpty);

        return results;
    }
}
