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

    public List<DiligenceRankItem> getSocialRankingOfDiligence(Member member) {
        return socialQueryRepository.findTop10MembersByDiligenceLevelAndGauge(member);
    }

    public List<CollectedPetRankItem> getSocialRankingOfCollection(Member member) {
        return socialQueryRepository.findTop10MembersByCollectedPetCnt(member);
    }

    public List<TodoAchievementRankItem> getSocialRankingOfDailyAchievement(Member member) {
        return socialQueryRepository.findTop10MembersByYesterdayAchievement(member);
    }

    public List<TodoAchievementRankItem> getSocialRankingOfWeeklyAchievement(Member member) {
        return socialQueryRepository.findTop10MembersByWeeklyAchievement(member);
    }

}
