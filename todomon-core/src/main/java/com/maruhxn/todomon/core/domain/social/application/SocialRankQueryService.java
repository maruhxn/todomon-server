package com.maruhxn.todomon.core.domain.social.application;

import com.maruhxn.todomon.core.domain.social.dto.response.CollectedPetRankItem;
import com.maruhxn.todomon.core.domain.social.dto.response.DiligenceRankItem;
import com.maruhxn.todomon.core.domain.social.dto.response.TodoAchievementRankItem;
import com.maruhxn.todomon.core.domain.social.implement.SocialRankReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SocialRankQueryService {

    private final SocialRankReader socialRankReader;

    @Cacheable(value = "socialDiligenceRankCache", key = "#memberId", cacheManager = "dailyCacheManager")
    public List<DiligenceRankItem> getSocialRankingOfDiligence(Long memberId) {
        log.debug("일관성 랭킹 조회 === 유저 아이디: {}", memberId);
        return socialRankReader.findTop10MembersByDiligenceLevelAndGauge(memberId);
    }

    @Cacheable(value = "socialCollectedPetRankCache", key = "#memberId", cacheManager = "dailyCacheManager")
    public List<CollectedPetRankItem> getSocialRankingOfCollection(Long memberId) {
        log.debug("펫 도감 랭킹 조회 === 유저 아이디: {}", memberId);
        return socialRankReader.findTop10MembersByCollectedPetCnt(memberId);
    }

    @Cacheable(value = "socialDailyAchievementRankCache", key = "#memberId", cacheManager = "dailyCacheManager")
    public List<TodoAchievementRankItem> getSocialRankingOfDailyAchievement(Long memberId) {
        log.debug("일간 투두 달성 랭킹 조회 === 유저 아이디: {}", memberId);
        return socialRankReader.findTop10MembersByYesterdayAchievement(memberId);
    }

    @Cacheable(value = "weeklyAchievementRankCache", key = "#memberId", cacheManager = "weeklyCacheManager")
    public List<TodoAchievementRankItem> getSocialRankingOfWeeklyAchievement(Long memberId) {
        log.debug("주간 투두 달성 랭킹 조회 === 유저 아이디: {}", memberId);
        return socialRankReader.findTop10MembersByWeeklyAchievement(memberId);
    }

}
