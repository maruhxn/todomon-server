package com.maruhxn.todomon.domain.member.dao;

import com.maruhxn.todomon.domain.member.dto.response.ProfileDto;
import com.maruhxn.todomon.domain.member.dto.response.SearchDto;
import com.maruhxn.todomon.domain.pet.domain.QPet;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.maruhxn.todomon.domain.member.domain.QDiligence.diligence;
import static com.maruhxn.todomon.domain.member.domain.QMember.member;
import static com.maruhxn.todomon.domain.member.domain.QTitleName.titleName;
import static com.maruhxn.todomon.domain.social.domain.QFollow.follow;

@Repository
@RequiredArgsConstructor
public class MemberQueryRepository {

    private final JPAQueryFactory query;

    public List<SearchDto> findMemberByKey(String key) {
        return query
                .select(
                        Projections.fields(SearchDto.class,
                                member.id.as("memberId"),
                                member.username
                        )
                )
                .from(member)
                .where(member.username.startsWith(key))
                .limit(5)
                .fetch();
    }

    // 유저명, 프로필사진, 이메일, 대표 펫, 팔로워 수, 팔로잉 수, 현재 칭호 반환
    public Optional<ProfileDto> getMemberProfileById(Long memberId) {
        QPet representPet = new QPet("representPet");

        ProfileDto profileDto = query
                .select(
                        Projections.fields(ProfileDto.class,
                                member.id,
                                member.username,
                                member.email,
                                member.profileImageUrl,
                                diligence.level,
                                diligence.gauge,
                                Projections.fields(ProfileDto.TitleNameItem.class,
                                        titleName.id,
                                        titleName.name,
                                        titleName.color
                                ).as("title"),
                                Projections.fields(ProfileDto.RepresentPetItem.class,
                                        representPet.id,
                                        representPet.name,
                                        representPet.rarity,
                                        representPet.appearance,
                                        representPet.color,
                                        representPet.level
                                ).as("representPetItem"),
                                ExpressionUtils.as(
                                        JPAExpressions.select(follow.countDistinct())
                                                .from(follow)
                                                .where(follow.followee.id.eq(memberId)),
                                        "followerCnt"
                                ),
                                ExpressionUtils.as(
                                        JPAExpressions.select(follow.countDistinct())
                                                .from(follow)
                                                .where(follow.follower.id.eq(memberId)),
                                        "followingCnt"
                                )
                        )
                )
                .from(member)
                .join(member.diligence, diligence)
                .leftJoin(member.titleName, titleName)
                .leftJoin(member.representPet, representPet)
                .where(member.id.eq(memberId))
                .fetchOne();

        if (profileDto != null) {
            profileDto.setTitleNameItemToNullIfIsEmpty();
            profileDto.setRepresentPetItemToNullIfIsEmpty();
        }

        return Optional.ofNullable(profileDto);
    }
}
