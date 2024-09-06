package com.maruhxn.todomon.batch.chunk.writer;

import com.maruhxn.todomon.batch.vo.SendNotificationBatchVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.time.format.DateTimeFormatter;

@Slf4j
public class SendNotificationWriter implements ItemWriter<SendNotificationBatchVO> {


    public static final String MAIL_TIME_FORMAT = "yyyy년 MM월 dd일(E) a hh시 mm분";

    @Override
    public void write(Chunk<? extends SendNotificationBatchVO> chunk) throws Exception {
        chunk.getItems()
                .forEach(item -> log.info(
                                "Send Email: {} to {}",
                                String.format(
                                        "[TODOMON] %s님, 잠시 후 해야 할 일이 있습니다! : [%s] %s",
                                        item.getUsername(),
                                        item.getStartAt().format(DateTimeFormatter.ofPattern(MAIL_TIME_FORMAT)),
                                        item.getContent()
                                ),
                                item.getEmail()
                        )
                );
    }
}
