package com.maruhxn.todomon.domain.item.dao;

import com.maruhxn.todomon.domain.item.domain.InventoryItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
    Optional<InventoryItem> findByMember_IdAndItem_Id(Long memberId, Long itemId);

    @EntityGraph(attributePaths = {"item"})
    Optional<InventoryItem> findByMember_IdAndItem_Name(Long memberId, String itemName);

    @EntityGraph(attributePaths = {"item"})
    List<InventoryItem> findAllByMember_Id(Long memberId);
}
