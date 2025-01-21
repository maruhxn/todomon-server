package com.maruhxn.todomon.infra.payment;

import java.io.IOException;
import java.math.BigDecimal;

public interface PaymentProvider {

    void prepare(String orderId, BigDecimal totalAmount) throws IOException;

    void complete(String paymentId, BigDecimal totalAmount) throws IOException;

    void refund(String paymentId) throws IOException;
}
