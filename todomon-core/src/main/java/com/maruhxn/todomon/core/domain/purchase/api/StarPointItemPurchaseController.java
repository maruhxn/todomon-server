package com.maruhxn.todomon.core.domain.purchase.api;

import com.maruhxn.todomon.core.domain.purchase.application.StarPointItemPurchaseService;
import com.maruhxn.todomon.core.domain.purchase.dto.request.PurchaseStarPointItemReq;
import com.maruhxn.todomon.core.global.auth.model.TodomonOAuth2User;
import com.maruhxn.todomon.core.global.common.dto.response.BaseResponse;
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
public class StarPointItemPurchaseController {

    private final StarPointItemPurchaseService starPointItemPurchaseService;

    @PostMapping
    public BaseResponse purchaseStarPointItem(
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User,
            @RequestBody @Valid PurchaseStarPointItemReq req
    ) {
        starPointItemPurchaseService.requestToPurchaseStarPointItem(todomonOAuth2User.getId(), req);
        return new BaseResponse("⭐️ 아이템 구매 성공");
    }
}
