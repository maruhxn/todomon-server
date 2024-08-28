package com.maruhxn.todomon.domain.purchase.api;

import com.maruhxn.todomon.domain.purchase.application.PurchaseService;
import com.maruhxn.todomon.domain.purchase.dto.request.PaymentRequest;
import com.maruhxn.todomon.domain.purchase.dto.request.PreparePaymentRequest;
import com.maruhxn.todomon.domain.purchase.dto.request.PurchaseStarPointItemRequest;
import com.maruhxn.todomon.global.auth.model.TodomonOAuth2User;
import com.maruhxn.todomon.global.common.dto.response.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    /**
     * 결제 정보 사전 검증
     * 해당 주문번호의 결제 예정 금액을 사전에 등록한다.
     *
     * @param req
     * @return
     */
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
}
