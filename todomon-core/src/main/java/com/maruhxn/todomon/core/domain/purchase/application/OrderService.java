package com.maruhxn.todomon.core.domain.purchase.application;

import com.maruhxn.todomon.core.domain.purchase.dto.response.OrderItem;
import com.maruhxn.todomon.core.domain.purchase.implement.OrderReader;
import com.maruhxn.todomon.core.global.common.dto.PageItem;
import com.maruhxn.todomon.core.global.common.dto.request.PagingCond;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderReader orderReader;

    @Transactional(readOnly = true)
    public PageItem<OrderItem> getMyOrders(Long memberId, PagingCond pagingCond) {
        return PageItem.from(orderReader.findOrdersWithPaging(memberId, pagingCond));
    }
}
