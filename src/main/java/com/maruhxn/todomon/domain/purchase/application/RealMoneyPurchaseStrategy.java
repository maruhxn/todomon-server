package com.maruhxn.todomon.domain.purchase.application;

import com.maruhxn.todomon.domain.member.domain.Member;
import com.maruhxn.todomon.domain.purchase.dao.PaymentRepository;
import com.maruhxn.todomon.domain.purchase.domain.*;
import com.maruhxn.todomon.domain.purchase.dto.request.PaymentRequest;
import com.maruhxn.todomon.domain.purchase.dto.request.PreparePaymentRequest;
import com.maruhxn.todomon.global.error.ErrorCode;
import com.maruhxn.todomon.global.error.exception.BadRequestException;
import com.maruhxn.todomon.global.error.exception.InternalServerException;
import com.siot.IamportRestClient.IamportClient;
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
    public void preValidate(Member member, Item item, PreparePaymentRequest req) throws Exception {
        if (isNotRealMoneyItem(item)) throw new BadRequestException(ErrorCode.BAD_REQUEST);
        PrepareData prepareData = new PrepareData(req.getMerchant_uid(), req.getAmount());
        IamportResponse<Prepare> response = iamportClient.postPrepare(prepareData);
        // IMP.request_pay()에 전달된 merchant_uid 가 일치하는 주문의 결제금액이 다를 경우 PG사 결제창 호출이 중단됨.
        System.out.println("response = " + response);
        if (response.getCode() != 0) {
            throw new InternalServerException(ErrorCode.INTERNAL_ERROR, "결제 정보 사전 등록 중 에러" + response.getMessage());
        }
    }

    @Override
    public void postValidate(Member member, Order order, PaymentRequest req) throws Exception {
        if (req.getImp_uid() == null) throw new BadRequestException(ErrorCode.BAD_REQUEST, "imp_uid 값은 비어있을 수 없습니다.");
        if (isNotRealMoneyItem(order.getItem())) throw new BadRequestException(ErrorCode.BAD_REQUEST);

        IamportResponse<Payment> response = iamportClient.paymentByImpUid(req.getImp_uid());
        Payment payment = response.getResponse();

        if (!Objects.equals(payment.getAmount(), new BigDecimal(order.getTotalPrice()))) {
            throw new BadRequestException(ErrorCode.INVALID_PAYMENT_AMOUNT_ERROR);
        }

        TodomonPayment todomonPayment = TodomonPayment.builder()
                .status(PaymentStatus.OK)
                .impUid(req.getImp_uid())
                .order(order)
                .build();
        // 결제 정보 저장
        paymentRepository.save(todomonPayment);
    }

    private static boolean isNotRealMoneyItem(Item item) {
        return !item.getMoneyType().equals(MoneyType.REAL_MONEY);
    }
}
