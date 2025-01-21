package com.maruhxn.todomon.core.domain.purchase.application;

import com.maruhxn.todomon.core.domain.item.domain.Item;
import com.maruhxn.todomon.core.domain.item.domain.MoneyType;
import com.maruhxn.todomon.core.domain.item.implement.ItemReader;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.member.implement.MemberReader;
import com.maruhxn.todomon.core.domain.purchase.dto.request.PurchaseStarPointItemReq;
import com.maruhxn.todomon.core.domain.purchase.implement.PurchaseManager;
import com.maruhxn.todomon.core.domain.purchase.implement.StarPointPurchaseHistoryAppender;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.BadRequestException;
import com.maruhxn.todomon.core.global.error.exception.ForbiddenException;
import com.maruhxn.todomon.core.global.error.exception.InternalServerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class StarPointItemPurchaseService {

    private final MemberReader memberReader;
    private final StarPointPurchaseHistoryAppender starPointPurchaseHistoryAppender;
    private final PurchaseManager purchaseManager;
    private final ItemReader itemReader;

    public void requestToPurchaseStarPointItem(Long memberId, PurchaseStarPointItemReq req) {
        try {
            Member member = memberReader.findById(memberId);
            Item item = itemReader.findItemById(req.getItemId());
            if (this.isNotStarPointItem(item))
                throw new BadRequestException(ErrorCode.BAD_REQUEST);

            if (!Objects.equals(member.getId(), memberId))
                throw new BadRequestException(ErrorCode.UNAUTHORIZED);

            if (member.getStarPoint() < req.getAmount()) {
                throw new BadRequestException(ErrorCode.NOT_ENOUGH_STAR_POINT);
            }

            this.checkIsPremiumItemAndMemberSubscription(item, member);
            starPointPurchaseHistoryAppender.create(req.toEntity(member));
            purchaseManager.purchase(member, item, req.getQuantity());
            member.subtractStarPoint(req.getAmount());
        } catch (Exception e) {
            throw new InternalServerException(ErrorCode.PURCHASE_ERROR, e.getMessage());
        }
    }

    private boolean isNotStarPointItem(Item item) {
        return !item.getMoneyType().equals(MoneyType.STARPOINT);
    }

    private void checkIsPremiumItemAndMemberSubscription(Item item, Member member) {
        if (item.getIsPremium() && !member.isSubscribed()) {
            throw new ForbiddenException(ErrorCode.NOT_SUBSCRIPTION);
        }
    }
}
