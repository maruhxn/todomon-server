package com.maruhxn.todomon.core.domain.social.application;

import com.maruhxn.todomon.core.domain.social.dto.response.CollectedPetRankItem;
import com.maruhxn.todomon.core.domain.social.dto.response.DiligenceRankItem;
import com.maruhxn.todomon.core.domain.social.dto.response.TodoAchievementRankItem;
import com.maruhxn.todomon.core.domain.social.implement.OverallRankReader;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OverallRankQueryService {

    private final OverallRankReader overallRankReader;

    @Cacheable(value = "overallDiligenceRankCache", key = "'overall'", cacheManager = "dailyCacheManager")
    public List<DiligenceRankItem> getTop10MembersByDiligenceLevelAndGauge() {
        return overallRankReader.findTop10MembersByDiligenceLevelAndGauge();
    }

    @Cacheable(value = "overallCollectedPetRankCache", key = "'overall'", cacheManager = "dailyCacheManager")
    public List<CollectedPetRankItem> getTop10MembersByCollectedPetCnt() {
        return overallRankReader.findTop10MembersByCollectedPetCnt();
    }

    @Cacheable(value = "overallDailyAchievementRankCache", key = "'overall'", cacheManager = "dailyCacheManager")
    public List<TodoAchievementRankItem> getRankingOfDailyAchievement() {
        return overallRankReader.findTop10MembersByYesterdayAchievement();
    }

    @Cacheable(value = "overallWeeklyAchievementRankCache", key = "'overall'", cacheManager = "weeklyCacheManager")
    public List<TodoAchievementRankItem> getRankingOfWeeklyAchievement() {
        return overallRankReader.findTop10MembersByWeeklyAchievement();
    }
}
