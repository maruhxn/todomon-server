package com.maruhxn.todomon.domain.member.domain;

import com.maruhxn.todomon.domain.item.domain.Item;
import com.maruhxn.todomon.domain.purchase.domain.Order;
import com.maruhxn.todomon.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"member_id", "item_id"})
        }
)
public class InventoryItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", referencedColumnName = "id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    private Long quantity;  // 유저가 소유한 아이템의 수량

    @Builder
    public InventoryItem(Member member, Item item, Long quantity) {
        this.member = member;
        this.item = item;
        this.quantity = quantity;
    }

    public static InventoryItem of(Member member, Order order) {
        return InventoryItem.builder()
                .member(member)
                .item(order.getItem())
                .quantity(order.getQuantity())
                .build();
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public void addQuantity(Long quantity) {
        this.quantity = quantity;
    }
}
