package com.maruhxn.todomon.infra.payment.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class CancelData {

    private String imp_uid;

    private String merchant_uid;

    private BigDecimal amount;

    private BigDecimal tax_free;

    private BigDecimal vat_amount;

    private BigDecimal checksum;

    private String reason;

    private String refund_holder;

    private String refund_bank;

    private String refund_account;

    private String refund_tel;

    private Boolean retain_promotion;

    private ExtraRequesterEntry extra;

    public CancelData(String uid, boolean imp_uid_or_not) {
        if (imp_uid_or_not) {
            this.imp_uid = uid;
        } else {
            this.merchant_uid = uid;
        }
    }

    public CancelData(String uid, boolean imp_uid_or_not, BigDecimal amount) {
        this(uid, imp_uid_or_not);
        this.amount = amount;
    }

    public class ExtraRequesterEntry {

        private String requester;

        public ExtraRequesterEntry(String requester) {
            this.requester = requester;
        }

        public String getRequester() {
            return requester;
        }
    }
}