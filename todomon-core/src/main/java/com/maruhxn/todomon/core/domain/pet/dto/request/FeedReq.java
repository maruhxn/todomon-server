package com.maruhxn.todomon.core.domain.pet.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access =  AccessLevel.PROTECTED)
public class FeedReq {
    int foodCnt;

    public FeedReq(int foodCnt) {
        this.foodCnt = foodCnt;
    }
}
