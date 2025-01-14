package com.maruhxn.todomon.core.domain.item.domain.item_effect;

import com.maruhxn.todomon.core.domain.item.dto.request.ItemEffectReq;
import com.maruhxn.todomon.core.domain.member.application.TitleNameService;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.member.dto.request.UpsertTitleNameReq;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("upsertMemberTitleNameEffect")
@RequiredArgsConstructor
public class UpsertMemberTitleNameEffect implements ItemEffect {

    private final TitleNameService titleNameService;

    @Override
    public void applyEffect(Member member, ItemEffectReq request) {
        if (!(request instanceof UpsertTitleNameReq upsertTitleNameRequest)) {
            throw new BadRequestException(ErrorCode.BAD_REQUEST, "잘못된 요청 타입입니다.");
        }

        titleNameService.upsertTitleName(member, upsertTitleNameRequest);

    }
}
