package com.maruhxn.todomon.domain.social.dao;

import com.maruhxn.todomon.domain.member.domain.QMember;
import com.maruhxn.todomon.domain.social.dto.response.AbstractMemberInfoItem;
import com.maruhxn.todomon.domain.social.dto.response.ReceivedStarItem;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static com.maruhxn.todomon.domain.member.domain.QTitleName.titleName;
import static com.maruhxn.todomon.domain.social.domain.QStarTransaction.starTransaction;
import static com.maruhxn.todomon.domain.social.domain.StarTransactionStatus.SENT;

@Repository
@RequiredArgsConstructor
public class StarTransactionQueryRepository {

    private final JPAQueryFactory query;

    public boolean existsStarsCreatedWithinLast24Hours(Long senderId, Long receiverId, LocalDateTime startTime, LocalDateTime endTime) {
        Long count = query
                .select(starTransaction.count())
                .from(starTransaction)
                .where(
                        starTransaction.sender.id.eq(senderId)
                                .and(starTransaction.receiver.id.eq(receiverId))
                                .and(starTransaction.createdAt.between(startTime, endTime))
                )
                .fetchOne();

        return count != null && count > 0;
    }

    public Page<ReceivedStarItem> findReceivedStarWithPaging(Long receiverId, Pageable pageable) {
        QMember sender = new QMember("sender");

        List<ReceivedStarItem> results = query
                .select(
                        Projections.fields(ReceivedStarItem.class,
                                starTransaction.id,
                                sender.id.as("senderId"),
                                sender.username,
                                sender.profileImageUrl,
                                Projections.fields(AbstractMemberInfoItem.TitleNameItem.class,
                                        titleName.name,
                                        titleName.color
                                ).as("title")
                        )
                )
                .from(starTransaction)
                .join(starTransaction.sender, sender)
                .leftJoin(sender.titleName, titleName)
                .where(isSENT(), starTransaction.receiver.id.eq(receiverId))
                .orderBy(starTransaction.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = query
                .select(starTransaction.count())
                .from(starTransaction)
                .where(isSENT(), starTransaction.receiver.id.eq(receiverId));

        results.forEach(AbstractMemberInfoItem::setTitleToNullIfIsEmpty);

        return PageableExecutionUtils.getPage(results, pageable, countQuery::fetchOne);
    }

    private static BooleanExpression isSENT() {
        return starTransaction.status.eq(SENT);
    }
}
