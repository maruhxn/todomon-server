package com.maruhxn.todomon.core.domain.payment.dao;

import com.maruhxn.todomon.core.domain.payment.dto.response.OrderItem;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.maruhxn.todomon.core.domain.member.domain.QMember.member;
import static com.maruhxn.todomon.core.domain.payment.domain.QOrder.order;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

    private final JPAQueryFactory query;

    public Page<OrderItem> findOrdersByMemberIdWithPaging(Long memberId, Pageable pageable) {
        List<OrderItem> results = query
                .select(
                        Projections.fields(OrderItem.class,
                                order.totalPrice,
                                order.quantity,
                                order.merchantUid,
                                order.moneyType,
                                order.orderStatus,
                                order.updatedAt
                        )
                )
                .from(order)
                .join(order.member, member)
                .where(member.id.eq(memberId))
                .orderBy(order.updatedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = query
                .select(order.count())
                .from(order)
                .join(order.member, member).fetchJoin()
                .where(member.id.eq(memberId));

        return PageableExecutionUtils.getPage(results, pageable, countQuery::fetchOne);
    }

}
