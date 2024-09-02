package com.maruhxn.todomon.domain.item.dto.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.maruhxn.todomon.domain.member.dto.request.UpsertTitleNameRequest;
import com.maruhxn.todomon.domain.pet.dto.request.ChangePetNameRequest;

// 아이템이 인벤토리에 존재해야만 요청 가능한 Request의 공통 인터페이스
// Jackson 다형성 처리
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = UpsertTitleNameRequest.class, name = "upsertTitleName"),
        @JsonSubTypes.Type(value = ChangePetNameRequest.class, name = "changePetName")
})
public interface ItemEffectRequest {
}
