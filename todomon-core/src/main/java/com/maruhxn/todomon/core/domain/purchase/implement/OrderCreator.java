package com.maruhxn.todomon.core.domain.purchase.implement;

import com.maruhxn.todomon.core.domain.item.domain.Item;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.purchase.dao.OrderRepository;
import com.maruhxn.todomon.core.domain.purchase.domain.Order;
import com.maruhxn.todomon.core.domain.purchase.dto.request.PreparePaymentReq;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderCreator {

    private final OrderRepository orderRepository;

    public Order create(Item item, Member member, PreparePaymentReq req) {
        Order order = req.toOrder(item, member);
        return orderRepository.save(order);
    }
}
