package com.maruhxn.todomon.core.domain.payment.implement;

import com.maruhxn.todomon.core.domain.item.domain.Item;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.payment.dao.OrderRepository;
import com.maruhxn.todomon.core.domain.payment.domain.Order;
import com.maruhxn.todomon.core.domain.payment.dto.request.PreparePaymentReq;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.maruhxn.todomon.core.domain.payment.domain.OrderStatus.FAILED;

@Component
@RequiredArgsConstructor
public class OrderWriter {

    private final OrderRepository orderRepository;

    public Order create(Item item, Member member, PreparePaymentReq req) {
        Order order = req.toOrder(item, member);
        return orderRepository.save(order);
    }

    public Order createFailedOrder(Item item, Member member, PreparePaymentReq req) {
        Order order = req.toOrder(item, member);
        order.updateStatus(FAILED);
        return orderRepository.save(order);
    }

    public Order save(Order order) {
        return orderRepository.save(order);
    }
}
