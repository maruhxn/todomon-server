package com.maruhxn.todomon.batch.service;

import com.maruhxn.todomon.core.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public void addStarAndResetAchieveCnt(Member member) {
        // 멤버 starPoint 추가
        member.addStar(member.getScheduledReward());

        // 멤버 dailyAchievementCnt & scheduledReward 초기화
        member.resetDailyAchievement();
        member.resetScheduledReward();

        memberRepository.save(member);
    }
}
