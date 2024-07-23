package com.maruhxn.todomon.domain.social.dao;

import com.maruhxn.todomon.domain.social.domain.StarTransaction;
import com.maruhxn.todomon.domain.social.domain.StarTransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StarTransactionRepository extends JpaRepository<StarTransaction, Long> {
    List<StarTransaction> findAllByReceiver_IdAndStatus(Long receiverId, StarTransactionStatus status);
}
