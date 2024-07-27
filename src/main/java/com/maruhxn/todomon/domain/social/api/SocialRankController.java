package com.maruhxn.todomon.domain.social.api;


import com.maruhxn.todomon.domain.social.application.SocialRankQueryService;
import com.maruhxn.todomon.domain.social.dto.response.CollectedPetRankItem;
import com.maruhxn.todomon.domain.social.dto.response.DiligenceRankItem;
import com.maruhxn.todomon.domain.social.dto.response.TodoAchievementRankItem;
import com.maruhxn.todomon.global.auth.model.TodomonOAuth2User;
import com.maruhxn.todomon.global.common.dto.response.DataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/social/rank")
@RequiredArgsConstructor
public class SocialRankController {

    private final SocialRankQueryService socialRankQueryService;

    @GetMapping("/achievement/daily")
    public DataResponse<List<TodoAchievementRankItem>> getSocialRankingOfDailyAchievement(
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User
    ) {
        return DataResponse.of(
                "소셜 일간 투두 달성 랭킹 조회 성공",
                socialRankQueryService.getSocialRankingOfDailyAchievement(todomonOAuth2User.getMember())
        );
    }

    // 소셜 주간 랭킹(가장 많은 업적 달성)
    @GetMapping("/achievement/weekly")
    public DataResponse<List<TodoAchievementRankItem>> getSocialRankingOfWeeklyAchievement(
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User
    ) {
        return DataResponse.of(
                "소셜 주간 투두 달성 랭킹 조회 성공",
                socialRankQueryService.getSocialRankingOfWeeklyAchievement(todomonOAuth2User.getMember())
        );
    }

    // 소셜 일관성 레벨 랭킹 조회
    @GetMapping("/diligence")
    public DataResponse<List<DiligenceRankItem>> getSocialRankingOfDiligence(
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User
    ) {
        return DataResponse.of(
                "소셜 일관성 레벨 랭킹 조회 성공",
                socialRankQueryService.getSocialRankingOfDiligence(todomonOAuth2User.getMember())
        );
    }

    // 소셜 도감 랭킹 조회 (획득한 펫의 수)
    @GetMapping("/collection")
    public DataResponse<List<CollectedPetRankItem>> getSocialRankingOfCollection(
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User
    ) {
        return DataResponse.of(
                "소셜 도감 랭킹 조회 성공",
                socialRankQueryService.getSocialRankingOfCollection(todomonOAuth2User.getMember())
        );
    }
}
