package com.maruhxn.todomon.core.domain.purchase.api;

import com.maruhxn.todomon.core.domain.purchase.application.PurchaseService;
import com.maruhxn.todomon.core.domain.purchase.dto.request.PaymentRequest;
import com.maruhxn.todomon.core.domain.purchase.dto.request.PreparePaymentRequest;
import com.maruhxn.todomon.core.domain.purchase.dto.request.PurchaseStarPointItemRequest;
import com.maruhxn.todomon.core.global.auth.model.TodomonOAuth2User;
import com.maruhxn.todomon.core.global.common.dto.response.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/purchase")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    @PostMapping("/starPoint-item")
    public BaseResponse purchaseStarPointItem(
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User,
            @RequestBody @Valid PurchaseStarPointItemRequest req
    ) {
        purchaseService.purchaseStarPointItem(todomonOAuth2User.getId(), req);
        return new BaseResponse("⭐️ 아이템 결제 요청 성공");
    }

    @PostMapping("/payment/prepare")
    public BaseResponse preparePayment(
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User,
            @RequestBody @Valid PreparePaymentRequest req
    ) {
        purchaseService.preparePayment(todomonOAuth2User.getId(), req);
        return new BaseResponse("사전 검증 정보 등록 성공");
    }

    @PostMapping("/payment/validate")
    public BaseResponse validatePayment(
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User,
            @RequestBody @Valid PaymentRequest req
    ) {
        purchaseService.verifyPayment(todomonOAuth2User.getId(), req);
        return new BaseResponse("결제 성공");
    }

    @PostMapping("/payment/cancel/{orderId}")
    public BaseResponse cancelPayment(
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User,
            @PathVariable Long orderId
    ) {
        purchaseService.cancelPayment(todomonOAuth2User.getId(), orderId);
        return new BaseResponse("결제 취소 성공");
    }
}
