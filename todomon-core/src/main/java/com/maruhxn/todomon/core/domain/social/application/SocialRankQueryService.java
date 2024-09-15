package com.maruhxn.todomon.core.domain.social.application;

import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.social.dao.SocialQueryRepository;
import com.maruhxn.todomon.core.domain.social.dto.response.CollectedPetRankItem;
import com.maruhxn.todomon.core.domain.social.dto.response.DiligenceRankItem;
import com.maruhxn.todomon.core.domain.social.dto.response.TodoAchievementRankItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SocialRankQueryService {

    private final SocialQueryRepository socialQueryRepository;

    public List<DiligenceRankItem> getSocialRankingOfDiligence(Long memberId) {
        return socialQueryRepository.findTop10MembersByDiligenceLevelAndGauge(memberId);
    }

    public List<CollectedPetRankItem> getSocialRankingOfCollection(Long memberId) {
        return socialQueryRepository.findTop10MembersByCollectedPetCnt(memberId);
    }

    public List<TodoAchievementRankItem> getSocialRankingOfDailyAchievement(Long memberId) {
        return socialQueryRepository.findTop10MembersByYesterdayAchievement(memberId);
    }

    public List<TodoAchievementRankItem> getSocialRankingOfWeeklyAchievement(Long memberId) {
        return socialQueryRepository.findTop10MembersByWeeklyAchievement(memberId);
    }

}
