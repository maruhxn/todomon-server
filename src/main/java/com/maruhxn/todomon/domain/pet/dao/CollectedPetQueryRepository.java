package com.maruhxn.todomon.domain.pet.dao;

import com.maruhxn.todomon.domain.social.dto.response.CollectedPetRankItem;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.maruhxn.todomon.domain.member.domain.QMember.member;
import static com.maruhxn.todomon.domain.pet.domain.QCollectedPet.collectedPet;

@Repository
@RequiredArgsConstructor
public class CollectedPetQueryRepository {

    private final JPAQueryFactory query;


    public List<CollectedPetRankItem> findTop10MembersByCollectedPetCnt() {
        return query.select(
                        Projections.fields(CollectedPetRankItem.class,
                                member.id,
                                member.username,
                                member.profileImageUrl,
                                collectedPet.id.count().intValue().as("petCnt"),
                                collectedPet.createdAt.max().as("lastCollectedAt")
                        )
                )
                .from(member)
                .leftJoin(member.collectedPets, collectedPet)
                .groupBy(member.id, member.username, member.profileImageUrl)
                .orderBy(collectedPet.id.count().desc(), collectedPet.createdAt.max().desc(), member.createdAt.asc())
                .limit(10)
                .fetch();
    }

}
