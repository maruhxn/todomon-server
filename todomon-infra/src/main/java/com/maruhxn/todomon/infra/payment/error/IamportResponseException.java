package com.maruhxn.todomon.infra.payment.error;

public class IamportResponseException extends RuntimeException {
    private int statusCode;

    public IamportResponseException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

}