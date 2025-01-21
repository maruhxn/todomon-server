package com.maruhxn.todomon.infra.mail;

import com.maruhxn.todomon.infra.mail.error.MailSendException;
import com.maruhxn.todomon.infra.mail.dto.PaymentResourceDTO;
import com.maruhxn.todomon.infra.mail.dto.SendNotificationBatchDTO;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GMailService implements MailService {

    @Value("${spring.mail.username}")
    private String sender;

    private final JavaMailSender javaMailSender;
    private final EmailMessageBuilder emailMessageBuilder;

    @Override
    @Async
    public void sendEmail(String receiverEmail, String message) throws MailSendException {
        log.info("이메일 발송!");
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);
            //받는사람
            messageHelper.setFrom(sender);
            messageHelper.setTo(receiverEmail);
            messageHelper.setSubject(SUBJECT);
            messageHelper.setText(message);
            javaMailSender.send(messageHelper.getMimeMessage());
        } catch (MessagingException e) {
            throw new MailSendException(e);
        }
    }

    @Override
    public void sendNotification(SendNotificationBatchDTO dto) throws MailSendException {
        String notificationMessage = emailMessageBuilder.getNotificationMessage(dto);
        sendEmail(dto.getEmail(), notificationMessage);
    }

    @Override
    public void sendPaymentMail(PaymentResourceDTO dto) {
        String message = emailMessageBuilder.getPaymentMessage(dto);
        sendEmail(dto.getEmail(), message);
    }

    @Override
    public void sendRefundMail(PaymentResourceDTO dto) {
        String message = emailMessageBuilder.getRefundMessage(dto);
        sendEmail(dto.getEmail(), message);
    }
}
