package com.maruhxn.todomon.core.domain.payment.dao;

import com.maruhxn.todomon.core.domain.payment.domain.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, String> {

    @EntityGraph(attributePaths = {"item", "member"})
    Optional<Order> findByMerchantUid(String merchantUid);

}
