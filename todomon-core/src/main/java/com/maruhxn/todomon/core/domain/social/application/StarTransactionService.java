package com.maruhxn.todomon.core.domain.social.application;

import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.member.implement.MemberReader;
import com.maruhxn.todomon.core.domain.social.domain.StarTransaction;
import com.maruhxn.todomon.core.domain.social.dto.response.ReceivedStarItem;
import com.maruhxn.todomon.core.domain.social.implement.FollowValidator;
import com.maruhxn.todomon.core.domain.social.implement.StarTransactionManager;
import com.maruhxn.todomon.core.global.common.dto.PageItem;
import com.maruhxn.todomon.core.global.common.dto.request.PagingCond;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.maruhxn.todomon.core.domain.social.domain.StarTransactionStatus.RECEIVED;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class StarTransactionService {

    private final MemberReader memberReader;
    private final FollowValidator followValidator;
    private final StarTransactionManager starTransactionManager;

    public void sendStar(Long senderId, Long receiverId, LocalDateTime now) {
        starTransactionManager.checkIsSelfTransaction(senderId, receiverId);
        starTransactionManager.checkIsAlreadySent(senderId, receiverId, now);
        Member me = memberReader.findById(senderId);
        Member receiver = memberReader.findById(receiverId, "수신자 정보가 존재하지 않습니다.");
        followValidator.checkIsFollow(senderId, receiverId);
        starTransactionManager.createTransaction(StarTransaction.createTransaction(me, receiver));
        log.info("⭐️전송 === 발신자: {}, 수신자: {}", senderId, receiverId);
    }

    public void receiveOneStar(Long receiverId, Long transactionId) {
        StarTransaction transaction = starTransactionManager.findByIdAndReceiverId(transactionId, receiverId);
        if (transaction.isReceived()) throw new BadRequestException(ErrorCode.ALREADY_RECEIVED);
        memberReader.findById(receiverId, "수신자 정보가 존재하지 않습니다.")
                .addStar(1);
        transaction.updateStatus(RECEIVED);
        log.info("⭐단건 받기 === 유저 아이디: {}, 트랜잭션 아이디: {}", receiverId, transactionId);
    }

    public void receiveAllStars(Long receiverId) {
        List<StarTransaction> transactions = starTransactionManager.findAllSentTransactions(receiverId);
        transactions.forEach(tx -> tx.updateStatus(RECEIVED));
        memberReader.findById(receiverId).addStar(transactions.size());
        log.info("⭐️모두 받기 === 유저 아이디: {}", receiverId);
    }

    @Transactional(readOnly = true)
    public PageItem<ReceivedStarItem> getReceivedStars(Long memberId, PagingCond pagingCond) {
        log.debug("받은 ⭐조회 === 유저 아이디: {}, 페이지: {}", memberId, pagingCond.getPageNumber());
        memberReader.findById(memberId);
        return PageItem.from(starTransactionManager.findAllSentTransactionsWithPaging(memberId, pagingCond));
    }
}
