package com.maruhxn.todomon.domain.social.application;

import com.maruhxn.todomon.domain.member.dao.MemberQueryRepository;
import com.maruhxn.todomon.domain.pet.dao.CollectedPetQueryRepository;
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

    private final MemberQueryRepository memberQueryRepository;
    private final CollectedPetQueryRepository collectedPetQueryRepository;

    public List<DiligenceRankItem> getTop10MembersByDiligenceLevelAndGauge() {
        return memberQueryRepository.findTop10MembersByDiligenceLevelAndGauge();
    }

    public List<CollectedPetRankItem> getTop10MembersByCollectedPetCnt() {
        return collectedPetQueryRepository.findTop10MembersByCollectedPetCnt();
    }

    public List<TodoAchievementRankItem> getRankingOfDailyAchievement() {
        return memberQueryRepository.findTop10MembersByYesterdayAchievement();
    }

    public List<TodoAchievementRankItem> getRankingOfWeeklyAchievement() {
        return memberQueryRepository.findTop10MembersByWeeklyAchievement();
    }
}
