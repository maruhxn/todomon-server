package com.maruhxn.todomon.domain.member.dao;

import com.maruhxn.todomon.domain.member.domain.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
    Optional<InventoryItem> findByMember_IdAndItem_Id(Long memberId, Long itemId);
}
