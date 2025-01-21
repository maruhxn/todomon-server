package com.maruhxn.todomon.infra.payment;

import com.maruhxn.todomon.infra.payment.client.IamportClient;
import com.maruhxn.todomon.infra.payment.dto.request.CancelData;
import com.maruhxn.todomon.infra.payment.dto.request.PrepareData;
import com.maruhxn.todomon.infra.payment.dto.response.IamportResponse;
import com.maruhxn.todomon.infra.payment.dto.response.Payment;
import com.maruhxn.todomon.infra.payment.dto.response.Prepare;
import com.maruhxn.todomon.infra.payment.error.InvalidPaymentAmountException;
import com.maruhxn.todomon.infra.payment.error.PrepareException;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Objects;

public class IamportProvider implements PaymentProvider {

    private final IamportClient iamportClient;

    public IamportProvider(IamportClient iamportClient) {
        this.iamportClient = iamportClient;
    }

    @Override
    public void prepare(String orderId, BigDecimal totalAmount) throws IOException {
        PrepareData prepareData = PrepareData.builder()
                .merchant_uid(orderId)
                .amount(totalAmount)
                .build();

        IamportResponse<Prepare> response = iamportClient.prepare(prepareData);

        // IMP.request_pay()에 전달된 merchant_uid 가 일치하는 주문의 결제금액이 다를 경우 PG사 결제창 호출이 중단됨.
        if (response.getCode() != 0) {
            throw new PrepareException();
        }
    }

    @Override
    public void complete(String paymentId, BigDecimal totalAmount) throws IOException {
        if (!StringUtils.hasText(paymentId)) {
            throw new IllegalArgumentException("포트원 결제 아이디 값은 비어있을 수 없습니다.");
        }

        Payment paymentResponse = iamportClient.paymentByImpUid(paymentId).getResponse();
        if (!Objects.equals(paymentResponse.getAmount(), totalAmount)) {
            throw new InvalidPaymentAmountException();
        }
    }

    @Override
    public void refund(String paymentId) throws IOException {
        if (!StringUtils.hasText(paymentId)) {
            throw new IllegalArgumentException("포트원 결제 아이디 값은 비어있을 수 없습니다.");
        }

        IamportResponse<Payment> response = iamportClient.paymentByImpUid(paymentId);
        CancelData cancelData = new CancelData(response.getResponse().getImp_uid(), true);
        iamportClient.cancelPaymentByImpUid(cancelData);
    }
}
