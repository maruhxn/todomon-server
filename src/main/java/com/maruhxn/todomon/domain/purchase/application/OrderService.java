package com.maruhxn.todomon.domain.purchase.application;

import com.maruhxn.todomon.domain.member.domain.Member;
import com.maruhxn.todomon.domain.purchase.dao.OrderRepository;
import com.maruhxn.todomon.domain.item.domain.Item;
import com.maruhxn.todomon.domain.purchase.domain.Order;
import com.maruhxn.todomon.domain.purchase.dto.request.PreparePaymentRequest;
import com.maruhxn.todomon.domain.purchase.dto.response.OrderItem;
import com.maruhxn.todomon.global.error.ErrorCode;
import com.maruhxn.todomon.global.error.exception.NotFoundException;
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

    public List<OrderItem> getMyOrders(Member member) {
        List<Order> orders = orderRepository.findAllByMember_IdOrderByUpdatedAtDesc(member.getId());
        return orders.stream().map(OrderItem::from).toList();
    }
}
