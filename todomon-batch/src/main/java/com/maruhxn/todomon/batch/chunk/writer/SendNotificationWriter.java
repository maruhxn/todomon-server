package com.maruhxn.todomon.batch.chunk.writer;

import com.maruhxn.todomon.infra.mail.MailService;
import com.maruhxn.todomon.infra.mail.dto.SendNotificationBatchDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

@Slf4j
public class SendNotificationWriter implements ItemWriter<SendNotificationBatchDTO> {

    private final MailService mailService;

    public static final String MAIL_TIME_FORMAT = "yyyy년 MM월 dd일(E) a hh시 mm분";

    public SendNotificationWriter(MailService mailService) {
        this.mailService = mailService;
    }

    @Override
    public void write(Chunk<? extends SendNotificationBatchDTO> chunk) throws Exception {
        for (SendNotificationBatchDTO sendNotificationBatchDTO : chunk.getItems()) {
            mailService.sendNotification(sendNotificationBatchDTO);
        }
    }
}
