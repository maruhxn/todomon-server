package com.maruhxn.todomon.core.domain.purchase.application;

import com.maruhxn.todomon.core.domain.purchase.dto.response.OrderItem;
import com.maruhxn.todomon.core.domain.purchase.implement.OrderReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderReader orderReader;

    @Transactional(readOnly = true)
    public List<OrderItem> getMyOrders(Long memberId) {
        return orderReader.findAllByMemberId(memberId)
                .stream().map(OrderItem::from).toList();
    }
}
