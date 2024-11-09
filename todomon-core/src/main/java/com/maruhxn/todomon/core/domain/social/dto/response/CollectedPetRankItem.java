package com.maruhxn.todomon.core.domain.social.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class CollectedPetRankItem extends AbstractMemberInfoItem {

    private Long memberId;
    private int petCnt;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime lastCollectedAt;

    @Builder
    public CollectedPetRankItem(Long memberId, String username, String profileImageUrl, int petCnt, LocalDateTime lastCollectedAt, TitleNameItem title) {
        super(username, profileImageUrl, title);
        this.memberId = memberId;
        this.petCnt = petCnt;
        this.lastCollectedAt = lastCollectedAt;
    }
}
