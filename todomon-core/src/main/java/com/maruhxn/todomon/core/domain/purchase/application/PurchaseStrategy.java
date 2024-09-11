package com.maruhxn.todomon.core.domain.purchase.application;

import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.item.domain.Item;
import com.maruhxn.todomon.core.domain.purchase.domain.Order;
import com.maruhxn.todomon.core.domain.purchase.dto.request.PaymentRequest;
import com.maruhxn.todomon.core.domain.purchase.dto.request.PreparePaymentRequest;

public interface PurchaseStrategy {

    void preValidate(Member member, Item item, PreparePaymentRequest req) throws Exception;

    void postValidate(Member member, Order order, PaymentRequest req) throws Exception;

    void refund(Member member, Order order) throws Exception;
}
