package com.maruhxn.todomon.infra.mail;

import com.maruhxn.todomon.infra.mail.dto.PaymentResourceDTO;
import com.maruhxn.todomon.infra.mail.dto.SendNotificationBatchDTO;


public interface MailService {
    public static final String SUBJECT = "TODOMON";

    void sendEmail(String email, String message);

    void sendNotification(SendNotificationBatchDTO dto);

    void sendPaymentMail(PaymentResourceDTO dto);

    void sendRefundMail(PaymentResourceDTO dto);
}
