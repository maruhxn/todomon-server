package com.maruhxn.todomon.infra.mail;

import com.maruhxn.todomon.infra.error.MailSendException;
import com.maruhxn.todomon.infra.mail.dto.SendNotificationBatchDTO;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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
    public void sendEmail(String receiverEmail, String message) throws MailSendException {
        log.info("Send email to {}", receiverEmail);
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
}