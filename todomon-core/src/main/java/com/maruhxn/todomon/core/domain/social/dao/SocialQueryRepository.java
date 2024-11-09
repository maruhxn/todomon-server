package com.maruhxn.todomon.core.domain.social.dao;

import com.maruhxn.todomon.core.domain.social.domain.FollowRequestStatus;
import com.maruhxn.todomon.core.domain.social.dto.response.AbstractMemberInfoItem;
import com.maruhxn.todomon.core.domain.social.dto.response.CollectedPetRankItem;
import com.maruhxn.todomon.core.domain.social.dto.response.DiligenceRankItem;
import com.maruhxn.todomon.core.domain.social.dto.response.TodoAchievementRankItem;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.maruhxn.todomon.core.domain.member.domain.QDiligence.diligence;
import static com.maruhxn.todomon.core.domain.member.domain.QMember.member;
import static com.maruhxn.todomon.core.domain.member.domain.QTitleName.titleName;
import static com.maruhxn.todomon.core.domain.pet.domain.QCollectedPet.collectedPet;
import static com.maruhxn.todomon.core.domain.social.domain.QFollow.follow;
import static com.maruhxn.todomon.core.domain.todo.domain.QTodoAchievementHistory.todoAchievementHistory;

@Repository
@RequiredArgsConstructor
public class SocialQueryRepository {

    private final JPAQueryFactory query;

    public List<DiligenceRankItem> findTop10MembersByDiligenceLevelAndGauge(Long currentMemberId) {
        List<DiligenceRankItem> followeeRankings = query
                .select(
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
                .from(follow)
                .innerJoin(member).on(follow.followee.id.eq(member.id).and(isAccepted()).and(follow.follower.id.eq(currentMemberId)))
                .innerJoin(member.diligence, diligence)
                .leftJoin(member.titleName, titleName)
                .orderBy(diligence.level.desc(), diligence.gauge.desc(), member.createdAt.asc())
                .limit(10)
                .fetch();

        DiligenceRankItem myItem = query
                .select(
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
                .innerJoin(member.diligence, diligence)
                .leftJoin(member.titleName, titleName)
                .where(member.id.eq(currentMemberId))
                .fetchOne();

        List<DiligenceRankItem> results = concatAndSortTodoDiligenceRankItems(followeeRankings, myItem);

        results.forEach(AbstractMemberInfoItem::setTitleToNullIfIsEmpty);

        return results;
    }

    public List<CollectedPetRankItem> findTop10MembersByCollectedPetCnt(Long currentMemberId) {
        List<CollectedPetRankItem> followeeRankings = query.select(
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
                .from(follow)
                .innerJoin(member).on(follow.followee.id.eq(member.id).and(isAccepted()).and(follow.follower.id.eq(currentMemberId)))
                .leftJoin(member.collectedPets, collectedPet)
                .leftJoin(member.titleName, titleName)
                .groupBy(member.id)
                .orderBy(collectedPet.id.count().desc(), collectedPet.createdAt.max().desc(), member.createdAt.asc())
                .limit(10)
                .fetch();

        CollectedPetRankItem myItem = query.select(
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
                .where(member.id.eq(currentMemberId))
                .groupBy(member.id)
                .fetchOne();

        List<CollectedPetRankItem> results = concatAndSortTodoCollectedPetRankItems(followeeRankings, myItem);

        results.forEach(AbstractMemberInfoItem::setTitleToNullIfIsEmpty);

        return results;
    }

    public List<TodoAchievementRankItem> findTop10MembersByYesterdayAchievement(Long currentMemberId) {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        List<TodoAchievementRankItem> followeeRankings = query.select(
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
                .from(follow)
                .innerJoin(member).on(follow.followee.id.eq(member.id).and(isAccepted()).and(follow.follower.id.eq(currentMemberId)))
                .leftJoin(todoAchievementHistory).on(todoAchievementHistory.memberId.eq(member.id))
                .leftJoin(member.titleName, titleName)
                .where(todoAchievementHistory.date.eq(yesterday))
                .groupBy(member.id, todoAchievementHistory.cnt)
                .orderBy(todoAchievementHistory.cnt.desc(), todoAchievementHistory.createdAt.max().asc(), member.createdAt.asc())
                .limit(10)
                .fetch();

        TodoAchievementRankItem myItem = query.select(
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
                .from(member)
                .leftJoin(todoAchievementHistory).on(todoAchievementHistory.memberId.eq(member.id))
                .leftJoin(member.titleName, titleName)
                .groupBy(member.id, todoAchievementHistory.cnt)
                .where(todoAchievementHistory.date.eq(yesterday).and(member.id.eq(currentMemberId)))
                .fetchOne();

        List<TodoAchievementRankItem> results = concatAndSortTodoAchievementRankItems(followeeRankings, myItem);

        results.forEach(AbstractMemberInfoItem::setTitleToNullIfIsEmpty);

        return results;
    }

    private static BooleanExpression isAccepted() {
        return follow.status.eq(FollowRequestStatus.ACCEPTED);
    }

    public List<TodoAchievementRankItem> findTop10MembersByWeeklyAchievement(Long currentMemberId) {
        LocalDate startOfCurrentWeek = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate startOfLastWeek = startOfCurrentWeek.minusWeeks(1);
        LocalDate endOfLastWeek = startOfCurrentWeek.minusDays(1);

        List<TodoAchievementRankItem> followeeRankings = query.select(
                        Projections.fields(TodoAchievementRankItem.class,
                                member.id.as("memberId"),
                                member.username,
                                member.profileImageUrl,
                                todoAchievementHistory.cnt.sum().as("cnt"),
                                Projections.fields(AbstractMemberInfoItem.TitleNameItem.class,
                                        titleName.name,
                                        titleName.color
                                ).as("title")
                        )
                )
                .from(follow)
                .innerJoin(member).on(follow.followee.id.eq(member.id).and(isAccepted()).and(follow.follower.id.eq(currentMemberId)))
                .leftJoin(todoAchievementHistory).on(todoAchievementHistory.memberId.eq(member.id))
                .leftJoin(member.titleName, titleName)
                .where(todoAchievementHistory.date.between(startOfLastWeek, endOfLastWeek))
                .groupBy(member.id)
                .orderBy(todoAchievementHistory.cnt.sum().desc(), todoAchievementHistory.createdAt.max().asc(), member.createdAt.asc())
                .limit(10)
                .fetch();

        TodoAchievementRankItem myItem = query.select(
                        Projections.fields(TodoAchievementRankItem.class,
                                member.id.as("memberId"),
                                member.username,
                                member.profileImageUrl,
                                todoAchievementHistory.cnt.sum().as("cnt"),
                                Projections.fields(AbstractMemberInfoItem.TitleNameItem.class,
                                        titleName.name,
                                        titleName.color
                                ).as("title")
                        )
                )
                .from(member)
                .leftJoin(todoAchievementHistory).on(todoAchievementHistory.memberId.eq(member.id))
                .leftJoin(member.titleName, titleName)
                .groupBy(member.id)
                .where(todoAchievementHistory.date.between(startOfLastWeek, endOfLastWeek).and(member.id.eq(currentMemberId)))
                .fetchOne();

        List<TodoAchievementRankItem> results = concatAndSortTodoAchievementRankItems(followeeRankings, myItem);

        results.forEach(AbstractMemberInfoItem::setTitleToNullIfIsEmpty);

        return results;
    }

    private static List<CollectedPetRankItem> concatAndSortTodoCollectedPetRankItems(List<CollectedPetRankItem> followeeRankings, CollectedPetRankItem myItem) {
        // null 체크 및 리스트 초기화
        List<CollectedPetRankItem> combinedList = new ArrayList<>();

        // followeeRankings가 null이 아닐 경우 추가
        if (followeeRankings != null) {
            combinedList.addAll(followeeRankings);
        }

        // myItem이 null이 아닐 경우 추가
        if (myItem != null) {
            combinedList.add(myItem);
        }

        // petCnt 내림차순, 같다면 memberId 오름차순 기준 정렬
        List<CollectedPetRankItem> results = combinedList.stream()
                .sorted(Comparator.comparingInt(CollectedPetRankItem::getPetCnt)
                        .reversed() // petCnt를 기준으로 내림차순
                        .thenComparing(
                                CollectedPetRankItem::getLastCollectedAt,
                                Comparator.nullsLast(Comparator.reverseOrder()) // lastCollectedAt을 기준으로 내림차순, null은 마지막에 위치
                        )
                        .thenComparingLong(CollectedPetRankItem::getMemberId) // memberId를 기준으로 오름차순
                )
                .collect(Collectors.toList());

        if (results.size() > 10) {
            results.remove(results.size() - 1); // 마지막 요소 제거
        }

        return results;
    }

    private static List<DiligenceRankItem> concatAndSortTodoDiligenceRankItems(List<DiligenceRankItem> followeeRankings, DiligenceRankItem myItem) {
        // null 체크 및 리스트 초기화
        List<DiligenceRankItem> combinedList = new ArrayList<>();

        // followeeRankings가 null이 아닐 경우 추가
        if (followeeRankings != null) {
            combinedList.addAll(followeeRankings);
        }

        // myItem이 null이 아닐 경우 추가
        if (myItem != null) {
            combinedList.add(myItem);
        }

        // level 내림차순, 같다면 memberId 오름차순 기준 정렬
        List<DiligenceRankItem> results = combinedList.stream()
                .sorted(Comparator.comparingInt(DiligenceRankItem::getLevel)
                        .reversed() // cnt를 기준으로 내림차순
                        .thenComparingLong(DiligenceRankItem::getMemberId) // memberId를 기준으로 오름차순
                ).collect(Collectors.toList());

        if (results.size() > 10) {
            results.remove(results.size() - 1); // 마지막 요소 제거
        }

        return results;
    }

    private static List<TodoAchievementRankItem> concatAndSortTodoAchievementRankItems(List<TodoAchievementRankItem> followeeRankings, TodoAchievementRankItem myItem) {
        // null 체크 및 리스트 초기화
        List<TodoAchievementRankItem> combinedList = new ArrayList<>();

        // followeeRankings가 null이 아닐 경우 추가
        if (followeeRankings != null) {
            combinedList.addAll(followeeRankings);
        }

        // myItem이 null이 아닐 경우 추가
        if (myItem != null) {
            combinedList.add(myItem);
        }

        // cnt 내림차순, 같다면 memberId 오름차순 기준 정렬
        List<TodoAchievementRankItem> results = combinedList.stream()
                .sorted(Comparator.comparingLong(TodoAchievementRankItem::getCnt)
                        .reversed() // cnt를 기준으로 내림차순
                        .thenComparingLong(TodoAchievementRankItem::getMemberId) // memberId를 기준으로 오름차순
                ).collect(Collectors.toList());

        if (results.size() > 10) {
            results.remove(results.size() - 1); // 마지막 요소 제거
        }

        return results;
    }


    private BooleanExpression followerIsCurrentMember(Long currentMemberId) {
        return currentMemberId != null ? follow.follower.id.eq(currentMemberId) : null;
    }

    private BooleanExpression isCurrentMember(Long currentMemberId) {
        return currentMemberId != null ? member.id.eq(currentMemberId) : null;
    }
}

