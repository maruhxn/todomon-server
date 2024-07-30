package com.maruhxn.todomon.domain.social.api;

import com.maruhxn.todomon.domain.social.application.StarTransactionService;
import com.maruhxn.todomon.global.auth.model.TodomonOAuth2User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/social/stars")
@RequiredArgsConstructor
public class StarTransactionController {

    private final StarTransactionService starTransactionService;

    // 내가 팔로우하고 있는 사람에게 별을 보낼 수 있다.
    @PostMapping("/send/{memberId}")
    public void sendStar(
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User,
            @PathVariable("memberId") Long memberId
    ) {
        starTransactionService.sendStar(todomonOAuth2User.getMember(), memberId);
    }

    @PatchMapping("/receive/{transactionId}")
    public void receiveStar(
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User,
            @PathVariable("transactionId") Long transactionId
    ) {
        starTransactionService.receiveOneStar(todomonOAuth2User.getMember(), transactionId);
    }

    @PatchMapping("/receiveAll")
    public void receiveAllStars(
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User
    ) {
        starTransactionService.receiveAllStars(todomonOAuth2User.getMember());
    }
}
