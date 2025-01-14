package com.maruhxn.todomon.core.domain.item.domain.item_effect;

import com.maruhxn.todomon.core.domain.item.dto.request.ItemEffectReq;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("plusCarrotEffect")
@RequiredArgsConstructor
public class PlusCarrotEffect implements ItemEffect {
    @Override
    public void applyEffect(Member member, ItemEffectReq itemEffectReq) {
        member.addFood(10);
    }
}
