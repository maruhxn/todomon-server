package com.maruhxn.todomon.core.domain.social.implement;

import com.maruhxn.todomon.core.domain.social.dao.OverallRankQueryRepository;
import com.maruhxn.todomon.core.domain.social.dto.response.CollectedPetRankItem;
import com.maruhxn.todomon.core.domain.social.dto.response.DiligenceRankItem;
import com.maruhxn.todomon.core.domain.social.dto.response.TodoAchievementRankItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OverallRankReader {

    private final OverallRankQueryRepository overallRankQueryRepository;

    public List<DiligenceRankItem> findTop10MembersByDiligenceLevelAndGauge() {
        return overallRankQueryRepository.findTop10MembersByDiligenceLevelAndGauge();
    }

    public List<CollectedPetRankItem> findTop10MembersByCollectedPetCnt() {
        return overallRankQueryRepository.findTop10MembersByCollectedPetCnt();
    }

    public List<TodoAchievementRankItem> findTop10MembersByYesterdayAchievement() {
        return overallRankQueryRepository.findTop10MembersByYesterdayAchievement();
    }

    public List<TodoAchievementRankItem> findTop10MembersByWeeklyAchievement() {
        return overallRankQueryRepository.findTop10MembersByWeeklyAchievement();
    }
}
