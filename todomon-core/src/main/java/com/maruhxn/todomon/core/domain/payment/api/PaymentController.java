package com.maruhxn.todomon.core.domain.payment.api;

import com.maruhxn.todomon.core.domain.payment.application.PaymentService;
import com.maruhxn.todomon.core.domain.payment.dto.request.PaymentReq;
import com.maruhxn.todomon.core.domain.payment.dto.request.PreparePaymentReq;
import com.maruhxn.todomon.core.global.auth.model.TodomonOAuth2User;
import com.maruhxn.todomon.core.global.common.dto.response.BaseResponse;
import com.maruhxn.todomon.infra.mail.MailService;
import com.maruhxn.todomon.infra.mail.dto.PaymentResourceDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final MailService mailService;


    @PostMapping("/prepare")
    public BaseResponse preparePayment(
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User,
            @RequestBody @Valid PreparePaymentReq req
    ) {
        paymentService.preparePayment(todomonOAuth2User.getId(), req);
        return new BaseResponse("사전 검증 정보 등록 성공");
    }

    @PostMapping("/complete")
    public BaseResponse completePayment(
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User,
            @RequestBody @Valid PaymentReq req
    ) {
        paymentService.completePayment(todomonOAuth2User.getId(), req);
        return new BaseResponse("사후 검증 성공");
    }

    @PostMapping("/purchase-item/{merchant_uid}")
    public BaseResponse purchaseItem(
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User,
            @PathVariable(name = "merchant_uid") @Valid String merchantUid
    ) {
        PaymentResourceDTO dto = paymentService.purchaseItem(todomonOAuth2User.getId(), merchantUid);
        mailService.sendPaymentMail(dto);
        return new BaseResponse("구매 성공");
    }

    @PostMapping("/cancel/{merchant_uid}")
    public BaseResponse cancelPayment(
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User,
            @PathVariable(name = "merchant_uid") String merchantUid
    ) {
        PaymentResourceDTO dto = paymentService.cancelPayment(todomonOAuth2User.getId(), merchantUid);
        mailService.sendRefundMail(dto);
        return new BaseResponse("결제 취소 성공");
    }
}
