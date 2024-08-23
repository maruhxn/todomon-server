package com.maruhxn.todomon.global.common.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PagingCond {

    @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다.")
    private int pageNumber = 0;

    public PagingCond(int pageNumber) {
        this.pageNumber = pageNumber;
    }
}
