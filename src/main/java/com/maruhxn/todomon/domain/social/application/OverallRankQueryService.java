package com.maruhxn.todomon.domain.social.application;

import com.maruhxn.todomon.domain.social.dao.OverallRankQueryRepository;
import com.maruhxn.todomon.domain.social.dto.response.CollectedPetRankItem;
import com.maruhxn.todomon.domain.social.dto.response.DiligenceRankItem;
import com.maruhxn.todomon.domain.social.dto.response.TodoAchievementRankItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OverallRankQueryService {

    private final OverallRankQueryRepository overallRankQueryRepository;

    public List<DiligenceRankItem> getTop10MembersByDiligenceLevelAndGauge() {
        return overallRankQueryRepository.findTop10MembersByDiligenceLevelAndGauge();
    }

    public List<CollectedPetRankItem> getTop10MembersByCollectedPetCnt() {
        return overallRankQueryRepository.findTop10MembersByCollectedPetCnt();
    }

    public List<TodoAchievementRankItem> getRankingOfDailyAchievement() {
        return overallRankQueryRepository.findTop10MembersByYesterdayAchievement();
    }

    public List<TodoAchievementRankItem> getRankingOfWeeklyAchievement() {
        return overallRankQueryRepository.findTop10MembersByWeeklyAchievement();
    }
}
