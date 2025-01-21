package com.maruhxn.todomon.core.domain.payment.dto.response;

import com.maruhxn.todomon.core.domain.item.domain.MoneyType;
import com.maruhxn.todomon.core.domain.payment.domain.Order;
import com.maruhxn.todomon.core.domain.payment.domain.OrderStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class OrderItem {

    private String merchantUid;
    private Long totalPrice;
    private Long quantity;
    private MoneyType moneyType;
    private OrderStatus orderStatus;
    private LocalDateTime updatedAt;

    @Builder
    public OrderItem(Long totalPrice, Long quantity, String merchantUid, MoneyType moneyType, OrderStatus orderStatus, LocalDateTime updatedAt) {
        this.totalPrice = totalPrice;
        this.quantity = quantity;
        this.merchantUid = merchantUid;
        this.moneyType = moneyType;
        this.orderStatus = orderStatus;
        this.updatedAt = updatedAt;
    }

    public static OrderItem from(Order order) {
        return OrderItem.builder()
                .totalPrice(order.getTotalPrice())
                .quantity(order.getQuantity())
                .merchantUid(order.getMerchantUid())
                .moneyType(order.getMoneyType())
                .orderStatus(order.getOrderStatus())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
