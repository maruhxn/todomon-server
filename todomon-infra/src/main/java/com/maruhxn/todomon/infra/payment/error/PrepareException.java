package com.maruhxn.todomon.infra.payment.error;

public class PrepareException extends RuntimeException {

    public PrepareException() {
        super("결제 금액이 일치하지 않습니다.");
    }

    public PrepareException(String message) {
        super(message);
    }
}
