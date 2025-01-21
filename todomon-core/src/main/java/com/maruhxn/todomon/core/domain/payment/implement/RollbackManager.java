package com.maruhxn.todomon.core.domain.payment.implement;

import com.maruhxn.todomon.core.domain.item.domain.Item;
import com.maruhxn.todomon.core.domain.item.implement.ItemReader;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.member.implement.MemberReader;
import com.maruhxn.todomon.core.domain.payment.domain.Order;
import com.maruhxn.todomon.core.domain.payment.domain.TodomonPayment;
import com.maruhxn.todomon.core.domain.payment.dto.request.WebhookPayload;
import com.maruhxn.todomon.core.domain.payment.dto.request.PreparePaymentReq;
import com.maruhxn.todomon.core.global.error.exception.InternalServerException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static com.maruhxn.todomon.core.domain.payment.domain.OrderStatus.FAILED;
import static com.maruhxn.todomon.core.domain.payment.domain.PaymentStatus.CANCELED;

@Component
@RequiredArgsConstructor
public class RollbackManager {

    private final OrderWriter orderWriter;
    private final OrderReader orderReader;
    private final MemberReader memberReader;
    private final ItemReader itemReader;
    private final RefundProvider refundProvider;

    @Transactional(noRollbackFor = InternalServerException.class)
    public void prepareStageRollback(Long memberId, PreparePaymentReq req) {
        Member member = memberReader.findById(memberId);
        Item item = itemReader.findItemById(req.getItemId());
        orderWriter.createFailedOrder(item, member, req);
    }

    @Transactional(noRollbackFor = InternalServerException.class)
    public void completeStageRollback(TodomonPayment todomonPayment, WebhookPayload req) {
        Order order = orderReader.findByMerchantUid(req.getMerchant_uid());
        order.setPayment(todomonPayment);
        order.getPayment().updateStatus(CANCELED);
        order.updateStatus(FAILED);
        refundProvider.refund(order);
    }

    @Transactional(noRollbackFor = InternalServerException.class)
    public void purchaseStageRollback(String merchantUid) {
        Order order = orderReader.findByMerchantUid(merchantUid);
        TodomonPayment todomonPayment = order.getPayment();
        todomonPayment.updateStatus(CANCELED);
        order.updateStatus(FAILED);
        refundProvider.refund(order);
    }

}
