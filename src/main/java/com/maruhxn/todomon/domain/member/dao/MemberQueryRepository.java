package com.maruhxn.todomon.domain.member.dao;

import com.maruhxn.todomon.domain.social.dto.response.DiligenceRankItem;
import com.maruhxn.todomon.domain.social.dto.response.TodoAchievementRankItem;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import static com.maruhxn.todomon.domain.member.domain.QDiligence.diligence;
import static com.maruhxn.todomon.domain.member.domain.QMember.member;
import static com.maruhxn.todomon.domain.todo.domain.QTodoAchievementHistory.todoAchievementHistory;

@Repository
@RequiredArgsConstructor
public class MemberQueryRepository {

    private final JPAQueryFactory query;

    public List<DiligenceRankItem> findTop10MembersByDiligenceLevelAndGauge() {
        return query.select(
                        Projections.fields(DiligenceRankItem.class,
                                member.id,
                                member.username,
                                member.profileImageUrl,
                                diligence.level
                        )
                )
                .from(member)
                .join(member.diligence, diligence)
                .orderBy(diligence.level.desc(), diligence.gauge.desc(), member.createdAt.asc())
                .limit(10)
                .fetch();
    }

    public List<TodoAchievementRankItem> findTop10MembersByYesterdayAchievement() {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        return query.select(
                        Projections.fields(TodoAchievementRankItem.class,
                                member.id,
                                member.username,
                                member.profileImageUrl,
                                todoAchievementHistory.cnt
                        )
                )
                .from(todoAchievementHistory)
                .join(todoAchievementHistory.member, member)
                .where(todoAchievementHistory.date.eq(yesterday))
                .groupBy(member.id)
                .orderBy(todoAchievementHistory.cnt.sum().desc(), todoAchievementHistory.createdAt.max().desc(), member.createdAt.asc())
                .limit(10)
                .fetch();
    }

    public List<TodoAchievementRankItem> findTop10MembersByWeeklyAchievement() {
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
                .from(todoAchievementHistory)
                .join(todoAchievementHistory.member, member)
                .where(todoAchievementHistory.date.between(startOfLastWeek, endOfLastWeek))
                .groupBy(member.id)
                .orderBy(todoAchievementHistory.cnt.sum().desc(), todoAchievementHistory.createdAt.max().desc(), member.createdAt.asc())
                .limit(10)
                .fetch();
    }
}
