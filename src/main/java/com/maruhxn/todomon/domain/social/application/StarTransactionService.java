package com.maruhxn.todomon.domain.social.application;

import com.maruhxn.todomon.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.domain.member.domain.Member;
import com.maruhxn.todomon.domain.social.dao.StarTransactionRepository;
import com.maruhxn.todomon.domain.social.domain.StarTransaction;
import com.maruhxn.todomon.global.error.ErrorCode;
import com.maruhxn.todomon.global.error.exception.BadRequestException;
import com.maruhxn.todomon.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.maruhxn.todomon.domain.social.domain.StarTransactionStatus.RECEIVED;
import static com.maruhxn.todomon.domain.social.domain.StarTransactionStatus.SENT;

@Service
@Transactional
@RequiredArgsConstructor
public class StarTransactionService {

    private final MemberRepository memberRepository;
    private final StarTransactionRepository starTransactionRepository;
    private final FollowService followService;

    public void sendStar(Member sender, Long receiverId) {
        if (sender.getId() == receiverId) throw new BadRequestException(ErrorCode.BAD_REQUEST);

        // 수신자 존재하는지 확인
        Member receiver = memberRepository.findById(receiverId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_MEMBER, "수신자 정보가 존재하지 않습니다."));

        // 서로 팔로우하고 있는지 확인 -> 맞팔로우하지 않은 상대에게는 star를 보낼 수 없음
        followService.checkIsFollow(sender.getId(), receiverId);

        StarTransaction transaction = StarTransaction.createTransaction(sender, receiver);

        starTransactionRepository.save(transaction);
    }

    public void receiveOneStar(Member receiver, Long transactionId) {
        StarTransaction transaction = starTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_STAR_TRANSACTION));

        if (transaction.getStatus() == RECEIVED) throw new BadRequestException(ErrorCode.ALREADY_RECEIVED);

        receiver.addStar(1);
        transaction.updateStatus(RECEIVED);
    }

    public void receiveAllStars(Member receiver) {
        List<StarTransaction> transactions = starTransactionRepository.findAllByReceiver_IdAndStatus(receiver.getId(), SENT);

        transactions.forEach(tx -> {
            tx.updateStatus(RECEIVED);
        });

        receiver.addStar(transactions.size());
    }
}
