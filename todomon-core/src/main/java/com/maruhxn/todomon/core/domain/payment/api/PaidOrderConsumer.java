package com.maruhxn.todomon.core.domain.payment.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maruhxn.todomon.core.domain.payment.application.PaymentService;
import com.maruhxn.todomon.core.domain.payment.dto.message.PaidOrderMessage;
import com.maruhxn.todomon.infra.mail.MailService;
import com.maruhxn.todomon.infra.mail.dto.PaymentResourceDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static com.maruhxn.todomon.core.domain.payment.PaidOrderConstants.PAID_ORDER_TOPIC;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaidOrderConsumer {

    private final PaymentService paymentService;
    private final MailService mailService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = PAID_ORDER_TOPIC, groupId = "paid-order-consumer-group")
    public void consume(String message) throws JsonProcessingException {
        log.info("처리할 주문 데이터 정보: ====> {}", message);

        PaidOrderMessage paidOrderMessage = objectMapper.readValue(message, PaidOrderMessage.class);
        PaymentResourceDTO dto = paymentService
                .purchaseItem(paidOrderMessage.getMemberId(), paidOrderMessage.getMerchantUid());
        mailService.sendPaymentMail(dto);
    }
}
