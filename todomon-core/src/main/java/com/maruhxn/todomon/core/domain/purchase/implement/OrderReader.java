package com.maruhxn.todomon.core.domain.purchase.implement;

import com.maruhxn.todomon.core.domain.purchase.dao.OrderRepository;
import com.maruhxn.todomon.core.domain.purchase.domain.Order;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderReader {

    private final OrderRepository orderRepository;

    public Order findById(Long id) {
        return orderRepository.findByIdWithMember(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_ORDER));
    }

    public List<Order> findAllByMemberId(Long memberId) {
        return orderRepository.findAllByMember_IdOrderByUpdatedAtDesc(memberId);
    }

    public Order findByMerchantUid(String merchantUid) {
        return orderRepository.findByMerchantUid(merchantUid)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_ORDER));
    }
}
