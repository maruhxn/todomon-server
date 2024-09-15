package com.maruhxn.todomon.core.domain.purchase.application;

import com.maruhxn.todomon.core.domain.item.domain.Item;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.purchase.dao.OrderRepository;
import com.maruhxn.todomon.core.domain.purchase.domain.Order;
import com.maruhxn.todomon.core.domain.purchase.dto.request.PreparePaymentRequest;
import com.maruhxn.todomon.core.domain.purchase.dto.response.OrderItem;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    public Order createOrder(Member member, Item item, PreparePaymentRequest req) {
        Order order = Order.builder()
                .item(item)
                .member(member)
                .totalPrice(item.getPrice() * req.getQuantity())
                .quantity(req.getQuantity())
                .merchantUid(req.getMerchant_uid())
                .moneyType(item.getMoneyType())
                .build();

        return orderRepository.save(order);
    }

    public Order findByMerchant_uid(String merchantUid) {
        return orderRepository.findByMerchantUid(merchantUid)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_ORDER));
    }

    public List<OrderItem> getMyOrders(Long memberId) {
        List<Order> orders = orderRepository.findAllByMember_IdOrderByUpdatedAtDesc(memberId);
        return orders.stream().map(OrderItem::from).toList();
    }
}
