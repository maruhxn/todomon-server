package com.maruhxn.todomon.domain.social.api;

import com.maruhxn.todomon.domain.social.application.OverallRankQueryService;
import com.maruhxn.todomon.global.common.dto.response.DataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 팔로우 된 친구들 간 순위 확인
@RestController
@RequestMapping("/api/overall/rank")
@RequiredArgsConstructor
public class OverallRankController {

    private final OverallRankQueryService overallRankQueryService;

    // 전체 일간 랭킹(가장 많은 업적 달성)
    @GetMapping("/achievement/daily")
    public DataResponse<Object> getOverallRankingOfDailyAchievement() {
        return DataResponse.of("전체 일간 투두 달성 랭킹 조회 성공", overallRankQueryService.getRankingOfDailyAchievement());
    }

    // 전체 주간 랭킹(가장 많은 업적 달성)
    @GetMapping("/achievement/weekly")
    public DataResponse<Object> getOverallRankingOfWeeklyAchievement() {
        return DataResponse.of("전체 주간 투두 달성 랭킹 조회 성공", overallRankQueryService.getRankingOfWeeklyAchievement());
    }

    // 전체 일관성 레벨 랭킹 조회
    @GetMapping("/diligence")
    public DataResponse<Object> getOverallRankingOfDiligence() {
        return DataResponse.of("전체 일관성 레벨 랭킹 조회 성공", overallRankQueryService.getTop10MembersByDiligenceLevelAndGauge());
    }

    // 전체 도감 랭킹 조회 (획득한 펫의 수)
    @GetMapping("/collection")
    public DataResponse<Object> getOverallRankingOfCollection() {
        return DataResponse.of("전체 도감 랭킹 조회 성공", overallRankQueryService.getTop10MembersByCollectedPetCnt());
    }
}
