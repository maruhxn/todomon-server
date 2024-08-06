package com.maruhxn.todomon.domain.social.api;

import com.maruhxn.todomon.domain.social.application.StarTransactionService;
import com.maruhxn.todomon.global.auth.model.TodomonOAuth2User;
import com.maruhxn.todomon.global.common.dto.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/social/stars")
@RequiredArgsConstructor
public class StarTransactionController {

    private final StarTransactionService starTransactionService;

    // 내가 팔로우하고 있는 사람에게 별을 보낼 수 있다.
    @PostMapping("/send/{memberId}")
    @ResponseStatus(HttpStatus.CREATED)
    public BaseResponse sendStar(
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User,
            @PathVariable("memberId") Long memberId
    ) {
        starTransactionService.sendStar(todomonOAuth2User.getMember(), memberId);
        return new BaseResponse("별 보내기 성공 - " + memberId);
    }

    @PatchMapping("/receive/{transactionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void receiveStar(
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User,
            @PathVariable("transactionId") Long transactionId
    ) {
        starTransactionService.receiveOneStar(todomonOAuth2User.getMember(), transactionId);
    }

    @PatchMapping("/receiveAll")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void receiveAllStars(
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User
    ) {
        starTransactionService.receiveAllStars(todomonOAuth2User.getMember());
    }
}
