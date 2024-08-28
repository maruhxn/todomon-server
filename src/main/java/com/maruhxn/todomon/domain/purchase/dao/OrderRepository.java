package com.maruhxn.todomon.domain.purchase.dao;

import com.maruhxn.todomon.domain.purchase.domain.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"item"})
    Optional<Order> findByMerchantUid(String merchantUid);
}
