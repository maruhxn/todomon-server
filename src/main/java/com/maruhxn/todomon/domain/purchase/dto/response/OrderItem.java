package com.maruhxn.todomon.domain.purchase.dto.response;

import com.maruhxn.todomon.domain.purchase.domain.MoneyType;
import com.maruhxn.todomon.domain.purchase.domain.Order;
import com.maruhxn.todomon.domain.purchase.domain.OrderStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class OrderItem {

    private Long orderId;
    private Long totalPrice;
    private Long quantity;
    private String merchantUid;
    private MoneyType moneyType;
    private OrderStatus orderStatus;
    private LocalDateTime updatedAt;

    @Builder
    public OrderItem(Long orderId, Long totalPrice, Long quantity, String merchantUid, MoneyType moneyType, OrderStatus orderStatus, LocalDateTime updatedAt) {
        this.orderId = orderId;
        this.totalPrice = totalPrice;
        this.quantity = quantity;
        this.merchantUid = merchantUid;
        this.moneyType = moneyType;
        this.orderStatus = orderStatus;
        this.updatedAt = updatedAt;
    }

    public static OrderItem from(Order order) {
        return OrderItem.builder()
                .orderId(order.getId())
                .totalPrice(order.getTotalPrice())
                .quantity(order.getQuantity())
                .merchantUid(order.getMerchantUid())
                .moneyType(order.getMoneyType())
                .orderStatus(order.getOrderStatus())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
