package com.maruhxn.todomon.domain.item.dao;

import com.maruhxn.todomon.domain.item.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
}
