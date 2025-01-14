package com.maruhxn.todomon.core.domain.item.dto.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.maruhxn.todomon.core.domain.member.dto.request.UpsertTitleNameReq;
import com.maruhxn.todomon.core.domain.pet.dto.request.ChangePetNameReq;

// 아이템이 인벤토리에 존재해야만 요청 가능한 Request의 공통 인터페이스
// Jackson 다형성 처리
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = UpsertTitleNameReq.class, name = "upsertTitleName"),
        @JsonSubTypes.Type(value = ChangePetNameReq.class, name = "changePetName")
})
public interface ItemEffectReq {
}
