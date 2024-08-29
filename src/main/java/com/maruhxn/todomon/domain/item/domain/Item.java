package com.maruhxn.todomon.domain.item.domain;

import com.maruhxn.todomon.domain.item.dto.request.UpdateItemRequest;
import com.maruhxn.todomon.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static org.springframework.util.StringUtils.hasText;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item extends BaseEntity {

    @Column(nullable = false, length = 30, unique = true)
    private String name;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    private ItemType itemType;

    @Enumerated(EnumType.STRING)
    private MoneyType moneyType;

    @Column(nullable = false)
    private Long price; // 가격; 실제 돈 또는 starPoint에 따라 다르게 해석

    @Column(nullable = false)
    private Boolean isAvailable = true; // 구매 가능 여부

    @Builder
    public Item(String name, String description, ItemType itemType, MoneyType moneyType, Long price) {
        this.name = name;
        this.description = description;
        this.itemType = itemType;
        this.moneyType = moneyType;
        this.price = price;
        this.isAvailable = true;
    }

    public void update(UpdateItemRequest req) {
        if (hasText(req.getName())) {
            this.name = req.getName();
        }

        if (hasText(req.getDescription())) {
            this.description = req.getDescription();
        }

        if (req.getPrice() != null) {
            this.price = req.getPrice();
        }

        if (req.getItemType() != null) {
            this.itemType = req.getItemType();
        }

        if (req.getMoneyType() != null) {
            this.moneyType = req.getMoneyType();
        }

        if (req.getIsAvailable() != null) {
            this.isAvailable = req.getIsAvailable();
        }
    }
}
