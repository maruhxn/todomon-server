package com.maruhxn.todomon.infra.payment.error;

public class InvalidPaymentAmountException extends RuntimeException {

    public InvalidPaymentAmountException() {
        super("결제 금액이 일치하지 않습니다.");
    }

    public InvalidPaymentAmountException(String message) {
        super(message);
    }
}
