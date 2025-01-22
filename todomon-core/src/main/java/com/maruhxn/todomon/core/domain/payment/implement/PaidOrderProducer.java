package com.maruhxn.todomon.core.domain.payment.implement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maruhxn.todomon.core.domain.payment.dto.message.PaidOrderMessage;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.InternalServerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

import static com.maruhxn.todomon.core.domain.payment.PaidOrderConstants.PAID_ORDER_TOPIC;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaidOrderProducer {

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void send(Long memberId, String merchantUid) {
        PaidOrderMessage message = PaidOrderMessage.builder()
                .memberId(memberId)
                .merchantUid(merchantUid)
                .build();

        String messageStr = null;
        try {
            messageStr = objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            log.error("메시지 전환 실패 === 이유: {}", e.getMessage());
            throw new InternalServerException(ErrorCode.INTERNAL_ERROR, "메시지 전환 실패");
        }

        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(PAID_ORDER_TOPIC, messageStr);
        future.whenComplete(((result, ex) -> {
            if (ex != null) {
                StackTraceElement stackTraceElement = ex.getStackTrace()[0];
                log.error("메시지 적재 실패 === 메시지: {}, 이유: {}", ex.getMessage(), stackTraceElement);
            } else {
                log.info("메시지 적재 성공 === 오프셋: {}", result.getRecordMetadata().offset());
            }
        }));
    }
}
