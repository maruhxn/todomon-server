package com.maruhxn.todomon.core.domain.purchase.implement.strategy;

import com.maruhxn.todomon.core.domain.purchase.dao.PaymentRepository;
import com.maruhxn.todomon.core.domain.purchase.domain.Order;
import com.maruhxn.todomon.core.domain.purchase.domain.PaymentStatus;
import com.maruhxn.todomon.core.domain.purchase.domain.TodomonPayment;
import com.maruhxn.todomon.core.domain.purchase.dto.request.PaymentReq;
import com.maruhxn.todomon.core.domain.purchase.dto.request.PreparePaymentReq;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.BadRequestException;
import com.maruhxn.todomon.core.global.error.exception.InternalServerException;
import com.maruhxn.todomon.core.global.error.exception.NotFoundException;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.request.CancelData;
import com.siot.IamportRestClient.request.PrepareData;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import com.siot.IamportRestClient.response.Prepare;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class RealMoneyPurchaseStrategy implements PurchaseStrategy {

    private final IamportClient iamportClient;
    private final PaymentRepository paymentRepository;

    @Override
    public void preValidate(Order order, PreparePaymentReq req) throws Exception {
        PrepareData prepareData = new PrepareData(req.getMerchant_uid(), req.getAmount());
        IamportResponse<Prepare> response = iamportClient.postPrepare(prepareData);

        // IMP.request_pay()에 전달된 merchant_uid 가 일치하는 주문의 결제금액이 다를 경우 PG사 결제창 호출이 중단됨.
        if (response.getCode() != 0) {
            throw new InternalServerException(ErrorCode.INTERNAL_ERROR, "결제 정보 사전 등록 중 에러" + response.getMessage());
        }
    }

    @Override
    public void postValidate(Order order, PaymentReq req) throws Exception {
        if (req.getImp_uid() == null)
            throw new BadRequestException(ErrorCode.BAD_REQUEST, "포트원 결제 아이디 값은 비어있을 수 없습니다.");

        Payment paymentResponse = iamportClient.paymentByImpUid(req.getImp_uid()).getResponse();
        TodomonPayment todomonPayment = TodomonPayment.of(order, req.getImp_uid());
        if (!Objects.equals(paymentResponse.getAmount(), new BigDecimal(order.getTotalPrice()))) {
            todomonPayment.updateStatus(PaymentStatus.FAILED);
            throw new BadRequestException(ErrorCode.INVALID_PAYMENT_AMOUNT_ERROR);
        }
        paymentRepository.save(todomonPayment);
    }

    @Override
    public void refund(Order order) throws Exception {
        TodomonPayment todomonPayment = paymentRepository.findByMember_IdAndOrder_Id(order.getMember().getId(), order.getId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_PAYMENT));
        IamportResponse<Payment> response = iamportClient.paymentByImpUid(todomonPayment.getImpUid());
        CancelData cancelData = new CancelData(response.getResponse().getImpUid(), true);
        iamportClient.cancelPaymentByImpUid(cancelData);
        todomonPayment.updateStatus(PaymentStatus.REFUNDED);
    }
}
