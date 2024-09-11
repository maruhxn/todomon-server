package com.maruhxn.todomon.infra.mail;

import com.maruhxn.todomon.infra.mail.dto.SendNotificationBatchDTO;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class EmailMessageBuilder {

    public static final String MAIL_TIME_FORMAT = "yyyy년 MM월 dd일(E) a hh시 mm분";

    public String getNotificationMessage(SendNotificationBatchDTO item) {
        String content = String.format(
                "%s님, 잠시 후 해야 할 일이 있습니다! : [%s] %s",
                item.getUsername(),
                item.getStartAt().format(DateTimeFormatter.ofPattern(MAIL_TIME_FORMAT)),
                item.getContent()
        );
        return content;
    }
}
