package com.maruhxn.todomon.core.domain.item.domain;

import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.global.common.BaseEntity;
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

    public static InventoryItem of(Member member, Item item, Long quantity) {
        return InventoryItem.builder()
                .member(member)
                .item(item)
                .quantity(quantity)
                .build();
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public void addQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public void decreaseQuantity() {
        this.quantity -= 1;
    }
}
