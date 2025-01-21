package com.maruhxn.todomon.infra.payment.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class Payment {
    String imp_uid;
    String merchant_uid;
    String pay_method;
    String channel;
    String pg_provider;
    String emb_pg_provider;
    String pg_tid;
    String pg_id;
    Boolean escrow;
    String apply_num;
    String bank_code;
    String bank_name;
    String card_code;
    String card_name;
    String card_number;
    String card_issuer_code;
    String card_issuer_name;
    String card_publisher_code;
    String card_publisher_name;
    Integer card_quota;
    Integer card_type;
    String vbank_code;
    String vbank_name;
    String vbank_num;
    String vbank_holder;
    Long vbank_date;
    Long vbank_issued_at;
    String name;
    BigDecimal amount;
    BigDecimal cancel_amount;
    String currency;
    String buyer_name;
    String buyer_email;
    String buyer_tel;
    String buyer_addr;
    String buyer_postcode;
    String custom_data;
    String user_agent;
    String status;
    Long started_at;
    Long paid_at;
    Long failed_at;
    Long cancelled_at;
    String fail_reason;
    String cancel_reason;
    String receipt_url;
    PaymentCancelDetail[] cancel_history;
    String[] cancel_receipt_urls;
    Boolean cash_receipt_issued;
    String customer_uid;
    String customer_uid_usage;
    Object promotion;

    @Getter
    @NoArgsConstructor
    public class PaymentCancelDetail {
        String pg_tid;
        BigDecimal amount;
        Long cancelled_at;
        String reason;
        String cancellation_id;
        String receipt_url;
    }
}
