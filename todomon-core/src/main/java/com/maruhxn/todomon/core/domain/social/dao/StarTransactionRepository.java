package com.maruhxn.todomon.core.domain.social.dao;

import com.maruhxn.todomon.core.domain.social.domain.StarTransaction;
import com.maruhxn.todomon.core.domain.social.domain.StarTransactionStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StarTransactionRepository extends JpaRepository<StarTransaction, Long> {

    List<StarTransaction> findAllByReceiver_IdAndStatus(Long receiverId, StarTransactionStatus status);

    @EntityGraph(attributePaths = {"sender.titleName"})
    List<StarTransaction> findAllWithSenderByReceiver_IdAndStatus(Long receiverId, StarTransactionStatus status);

}
