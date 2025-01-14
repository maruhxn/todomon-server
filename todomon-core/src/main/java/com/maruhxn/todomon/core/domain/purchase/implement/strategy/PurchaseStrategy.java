package com.maruhxn.todomon.core.domain.purchase.implement.strategy;

import com.maruhxn.todomon.core.domain.purchase.domain.Order;
import com.maruhxn.todomon.core.domain.purchase.dto.request.PaymentReq;
import com.maruhxn.todomon.core.domain.purchase.dto.request.PreparePaymentReq;

public interface PurchaseStrategy {

    void preValidate(Order order, PreparePaymentReq req) throws Exception;

    void postValidate(Order order, PaymentReq req) throws Exception;

    void refund(Order order) throws Exception;
}
