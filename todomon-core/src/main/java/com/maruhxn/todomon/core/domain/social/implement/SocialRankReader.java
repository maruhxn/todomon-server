package com.maruhxn.todomon.core.domain.social.implement;

import com.maruhxn.todomon.core.domain.social.dao.SocialQueryRepository;
import com.maruhxn.todomon.core.domain.social.dto.response.CollectedPetRankItem;
import com.maruhxn.todomon.core.domain.social.dto.response.DiligenceRankItem;
import com.maruhxn.todomon.core.domain.social.dto.response.TodoAchievementRankItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SocialRankReader {

    private final SocialQueryRepository socialQueryRepository;

    public List<DiligenceRankItem> findTop10MembersByDiligenceLevelAndGauge(Long memberId) {
        return socialQueryRepository.findTop10MembersByDiligenceLevelAndGauge(memberId);
    }

    public List<CollectedPetRankItem> findTop10MembersByCollectedPetCnt(Long memberId) {
        return socialQueryRepository.findTop10MembersByCollectedPetCnt(memberId);
    }

    public List<TodoAchievementRankItem> findTop10MembersByYesterdayAchievement(Long memberId) {
        return socialQueryRepository.findTop10MembersByYesterdayAchievement(memberId);
    }

    public List<TodoAchievementRankItem> findTop10MembersByWeeklyAchievement(Long memberId) {
        return socialQueryRepository.findTop10MembersByWeeklyAchievement(memberId);
    }
}
