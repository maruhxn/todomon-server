package com.maruhxn.todomon.core.domain.purchase.application;

import com.maruhxn.todomon.core.domain.purchase.dao.OrderRepository;
import com.maruhxn.todomon.core.domain.purchase.domain.Order;
import com.maruhxn.todomon.core.domain.purchase.dto.response.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public List<OrderItem> getMyOrders(Long memberId) {
        List<Order> orders = orderRepository.findAllByMember_IdOrderByUpdatedAtDesc(memberId);
        return orders.stream().map(OrderItem::from).toList();
    }
}
