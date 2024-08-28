package com.maruhxn.todomon.domain.purchase.application;

import com.maruhxn.todomon.domain.member.domain.Member;
import com.maruhxn.todomon.domain.purchase.dao.OrderRepository;
import com.maruhxn.todomon.domain.purchase.domain.Item;
import com.maruhxn.todomon.domain.purchase.domain.Order;
import com.maruhxn.todomon.domain.purchase.dto.request.PreparePaymentRequest;
import com.maruhxn.todomon.global.error.ErrorCode;
import com.maruhxn.todomon.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    public void createOrder(Member member, Item item, PreparePaymentRequest req) {
        Order order = Order.builder()
                .item(item)
                .member(member)
                .totalPrice(item.getPrice() * req.getQuantity())
                .quantity(req.getQuantity())
                .merchantUid(req.getMerchant_uid())
                .build();

        orderRepository.save(order);
    }

    public Order findByMerchant_uid(String merchantUid) {
        return orderRepository.findByMerchantUid(merchantUid)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_ORDER));
    }
}
