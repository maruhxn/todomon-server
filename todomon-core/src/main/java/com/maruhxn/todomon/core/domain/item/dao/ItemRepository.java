package com.maruhxn.todomon.core.domain.item.dao;

import com.maruhxn.todomon.core.domain.item.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
}
