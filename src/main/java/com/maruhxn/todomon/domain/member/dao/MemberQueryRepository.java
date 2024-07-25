package com.maruhxn.todomon.domain.member.dao;

import com.maruhxn.todomon.domain.social.dto.response.DiligenceRankItem;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.maruhxn.todomon.domain.member.domain.QDiligence.diligence;
import static com.maruhxn.todomon.domain.member.domain.QMember.member;

@Repository
@RequiredArgsConstructor
public class MemberQueryRepository {

    private final JPAQueryFactory query;

    public List<DiligenceRankItem> findTop10MembersByDiligenceLevelAndGauge() {
        return query.select(
                        Projections.fields(DiligenceRankItem.class,
                                member.id,
                                member.username,
                                member.profileImageUrl,
                                diligence.level
                        )
                )
                .from(member)
                .join(member.diligence, diligence)
                .orderBy(diligence.level.desc(), diligence.gauge.desc(), member.createdAt.asc())
                .limit(10)
                .fetch();
    }
}
