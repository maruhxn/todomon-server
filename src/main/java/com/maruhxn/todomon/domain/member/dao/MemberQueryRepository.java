package com.maruhxn.todomon.domain.member.dao;

import com.maruhxn.todomon.domain.member.dto.response.ProfileDto;
import com.maruhxn.todomon.domain.pet.domain.QPet;
import com.maruhxn.todomon.domain.social.dto.response.DiligenceRankItem;
import com.maruhxn.todomon.domain.social.dto.response.TodoAchievementRankItem;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import static com.maruhxn.todomon.domain.member.domain.QDiligence.diligence;
import static com.maruhxn.todomon.domain.member.domain.QMember.member;
import static com.maruhxn.todomon.domain.member.domain.QTitleName.titleName;
import static com.maruhxn.todomon.domain.social.domain.QFollow.follow;
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
                .orderBy(todoAchievementHistory.cnt.sum().desc(), todoAchievementHistory.createdAt.max().asc(), member.createdAt.asc())
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
                .orderBy(todoAchievementHistory.cnt.sum().desc(), todoAchievementHistory.createdAt.max().asc(), member.createdAt.asc())
                .limit(10)
                .fetch();
    }

    // 유저명, 프로필사진, 이메일, 대표 펫, 팔로워 수, 팔로잉 수, 현재 칭호 반환
    public ProfileDto getMemberProfileById(Long memberId) {
        QPet representPet = new QPet("representPet");

        ProfileDto profileDto = query
                .select(
                        Projections.fields(ProfileDto.class,
                                member.id,
                                member.username,
                                member.email,
                                member.profileImageUrl,
                                diligence.level,
                                diligence.gauge,
                                titleName.name.as("titleName"),
                                titleName.color.as("titleColor"),
                                Projections.fields(ProfileDto.RepresentPetItem.class,
                                        representPet.id,
                                        representPet.name,
                                        representPet.rarity,
                                        representPet.appearance,
                                        representPet.color,
                                        representPet.level
                                ).as("representPetItem"),
                                ExpressionUtils.as(
                                        JPAExpressions.select(follow.countDistinct())
                                                .from(follow)
                                                .where(follow.followee.id.eq(memberId)),
                                        "followerCnt"
                                ),
                                ExpressionUtils.as(
                                        JPAExpressions.select(follow.countDistinct())
                                                .from(follow)
                                                .where(follow.follower.id.eq(memberId)),
                                        "followingCnt"
                                )
                        )
                )
                .from(member)
                .join(member.diligence, diligence)
                .leftJoin(member.titleName, titleName)
                .leftJoin(member.representPet, representPet)
                .where(member.id.eq(memberId))
                .fetchOne();
        
        if (profileDto != null && profileDto.getRepresentPetItem().getId() == null) {
            profileDto.setRepresentPetItemToNull();
        }

        return profileDto;
    }
}
