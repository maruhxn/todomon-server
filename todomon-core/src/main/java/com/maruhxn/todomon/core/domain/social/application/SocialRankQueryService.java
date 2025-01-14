package com.maruhxn.todomon.core.domain.social.application;

import com.maruhxn.todomon.core.domain.social.dto.response.CollectedPetRankItem;
import com.maruhxn.todomon.core.domain.social.dto.response.DiligenceRankItem;
import com.maruhxn.todomon.core.domain.social.dto.response.TodoAchievementRankItem;
import com.maruhxn.todomon.core.domain.social.implement.SocialRankReader;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SocialRankQueryService {

    private final SocialRankReader socialRankReader;

    @Cacheable(value = "socialDiligenceRankCache", key = "#memberId", cacheManager = "dailyCacheManager")
    public List<DiligenceRankItem> getSocialRankingOfDiligence(Long memberId) {
        return socialRankReader.findTop10MembersByDiligenceLevelAndGauge(memberId);
    }

    @Cacheable(value = "socialCollectedPetRankCache", key = "#memberId", cacheManager = "dailyCacheManager")
    public List<CollectedPetRankItem> getSocialRankingOfCollection(Long memberId) {
        return socialRankReader.findTop10MembersByCollectedPetCnt(memberId);
    }

    @Cacheable(value = "socialDailyAchievementRankCache", key = "#memberId", cacheManager = "dailyCacheManager")
    public List<TodoAchievementRankItem> getSocialRankingOfDailyAchievement(Long memberId) {
        return socialRankReader.findTop10MembersByYesterdayAchievement(memberId);
    }

    @Cacheable(value = "weeklyAchievementRankCache", key = "#memberId", cacheManager = "weeklyCacheManager")
    public List<TodoAchievementRankItem> getSocialRankingOfWeeklyAchievement(Long memberId) {
        return socialRankReader.findTop10MembersByWeeklyAchievement(memberId);
    }

}
