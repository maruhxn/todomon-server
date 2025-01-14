package com.maruhxn.todomon.core.domain.social.implement;

import com.maruhxn.todomon.core.domain.social.dao.StarTransactionQueryRepository;
import com.maruhxn.todomon.core.domain.social.dao.StarTransactionRepository;
import com.maruhxn.todomon.core.domain.social.domain.StarTransaction;
import com.maruhxn.todomon.core.domain.social.domain.StarTransactionStatus;
import com.maruhxn.todomon.core.domain.social.dto.response.ReceivedStarItem;
import com.maruhxn.todomon.core.global.common.dto.request.PagingCond;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.BadRequestException;
import com.maruhxn.todomon.core.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class StarTransactionManager {
    private final StarTransactionRepository starTransactionRepository;
    private final StarTransactionQueryRepository starTransactionQueryRepository;

    public void createTransaction(StarTransaction starTransaction) {
        starTransactionRepository.save(starTransaction);
    }

    public void checkIsSelfTransaction(Long senderId, Long receiverId) {
        if (Objects.equals(senderId, receiverId)) throw new BadRequestException(ErrorCode.BAD_REQUEST);
    }

    // 24시간 내로 이미 보낸 적이 있는지 확인
    public void checkIsAlreadySent(Long senderId, Long receiverId, LocalDateTime now) {
        if (starTransactionQueryRepository
                .existsStarsCreatedWithinLast24Hours(senderId, receiverId, now.minusHours(24), now))
            throw new BadRequestException(ErrorCode.ALREADY_SENT_STAR);
    }

    public StarTransaction findByIdAndReceiverId(Long id, Long receiverId) {
        return starTransactionRepository.findByIdAndReceiver_Id(id, receiverId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_STAR_TRANSACTION));
    }

    public List<StarTransaction> findAllSentTransactions(Long receiverId) {
        return starTransactionRepository.findAllByReceiver_IdAndStatus(receiverId, StarTransactionStatus.SENT);
    }

    public Page<ReceivedStarItem> findAllSentTransactionsWithPaging(Long memberId, PagingCond pagingCond) {
        PageRequest pageRequest = PageRequest.of(pagingCond.getPageNumber(), 10);
        return starTransactionQueryRepository
                .findReceivedStarWithPaging(memberId, pageRequest);
    }
}
