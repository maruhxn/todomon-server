package com.maruhxn.todomon.core.domain.payment.implement;

import com.maruhxn.todomon.core.domain.payment.domain.Order;
import com.maruhxn.todomon.core.domain.payment.domain.PaymentStatus;
import com.maruhxn.todomon.core.domain.payment.domain.TodomonPayment;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.InternalServerException;
import com.maruhxn.todomon.infra.mail.MailService;
import com.maruhxn.todomon.infra.payment.PaymentProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefundProvider {

    private final PaymentProvider paymentProvider;
    private final MailService mailService;

    @Transactional(noRollbackFor = InternalServerException.class)
    public void refund(Order order) {
        try {
            paymentProvider.refund(order.getPayment().getImpUid());
        } catch (Exception e) {
            TodomonPayment todomonPayment = order.getPayment();
            todomonPayment.updateStatus(PaymentStatus.REFUND_FAILED);
            mailService.sendEmail(order.getMember().getEmail(), "환불에 실패했습니다. 관리자에게 문의바랍니다.");
            log.error("환불 실패 === 유저 아이디: {}, 주문 아이디: {}, 이유: {}", order.getMember().getId(), order.getMerchantUid(), e.getMessage());
            throw new InternalServerException(ErrorCode.REFUND_FAIL, e.getMessage());
        }
    }
}
