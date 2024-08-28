package com.maruhxn.todomon.domain.purchase.api;

import com.maruhxn.todomon.domain.purchase.application.OrderService;
import com.maruhxn.todomon.domain.purchase.dto.response.OrderItem;
import com.maruhxn.todomon.global.auth.model.TodomonOAuth2User;
import com.maruhxn.todomon.global.common.dto.response.DataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // TODO: 페이징 조회
    @GetMapping
    public DataResponse<List<OrderItem>> getMyOrders(
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User
    ) {
        List<OrderItem> orderList = orderService.getMyOrders(todomonOAuth2User.getMember());
        return DataResponse.of("주문 내역 조회 성공", orderList);
    }
}
