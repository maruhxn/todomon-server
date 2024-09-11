package com.maruhxn.todomon.core.domain.purchase.dao;

import com.maruhxn.todomon.core.domain.purchase.domain.TodomonPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<TodomonPayment, Long> {

    Optional<TodomonPayment> findByMember_IdAndOrder_Id(Long memberId, Long orderId);

}
