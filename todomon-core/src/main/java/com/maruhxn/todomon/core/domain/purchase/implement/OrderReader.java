package com.maruhxn.todomon.core.domain.purchase.implement;

import com.maruhxn.todomon.core.domain.purchase.dao.OrderRepository;
import com.maruhxn.todomon.core.domain.purchase.domain.Order;
import com.maruhxn.todomon.core.domain.purchase.domain.OrderQueryRepository;
import com.maruhxn.todomon.core.domain.purchase.dto.response.OrderItem;
import com.maruhxn.todomon.core.global.common.dto.request.PagingCond;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderReader {

    private static final int PAGE_SIZE = 20;

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    public Order findById(Long id) {
        return orderRepository.findByIdWithMember(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_ORDER));
    }

    public Page<OrderItem> findOrdersWithPaging(Long memberId, PagingCond pagingCond) {
        PageRequest pageRequest = PageRequest.of(pagingCond.getPageNumber(), PAGE_SIZE);
        return orderQueryRepository.findOrdersByMemberIdWithPaging(memberId, pageRequest);
    }

    public Order findByMerchantUid(String merchantUid) {
        return orderRepository.findByMerchantUid(merchantUid)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_ORDER));
    }
}
