package com.maruhxn.todomon.core.domain.purchase.application.strategy;

import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.item.domain.Item;
import com.maruhxn.todomon.core.domain.purchase.domain.Order;
import com.maruhxn.todomon.core.domain.purchase.dto.request.PaymentReq;
import com.maruhxn.todomon.core.domain.purchase.dto.request.PreparePaymentReq;

public interface PurchaseStrategy {

    void preValidate(Member member, Item item, PreparePaymentReq req) throws Exception;

    void postValidate(Member member, Order order, PaymentReq req) throws Exception;

    void refund(Member member, Order order) throws Exception;
}
