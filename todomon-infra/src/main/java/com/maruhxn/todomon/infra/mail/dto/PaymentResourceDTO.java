package com.maruhxn.todomon.infra.mail.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaymentResourceDTO {

    private String email;
    private String itemName;
    private Long quantity;
    private Long totalPrice;

    @Builder
    public PaymentResourceDTO(String email, String itemName, Long quantity, Long totalPrice) {
        this.email = email;
        this.itemName = itemName;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
    }
}
