package com.maruhxn.todomon.core.domain.payment.api;

import com.maruhxn.todomon.core.domain.payment.application.OrderService;
import com.maruhxn.todomon.core.domain.payment.dto.response.OrderItem;
import com.maruhxn.todomon.core.global.auth.model.TodomonOAuth2User;
import com.maruhxn.todomon.core.global.common.dto.PageItem;
import com.maruhxn.todomon.core.global.common.dto.request.PagingCond;
import com.maruhxn.todomon.core.global.common.dto.response.DataResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public DataResponse<PageItem<OrderItem>> getMyOrders(
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User,
            @ModelAttribute @Valid PagingCond pagingCond
    ) {
        return DataResponse.of("주문 내역 조회 성공", orderService.getMyOrders(todomonOAuth2User.getId(), pagingCond));
    }
}
