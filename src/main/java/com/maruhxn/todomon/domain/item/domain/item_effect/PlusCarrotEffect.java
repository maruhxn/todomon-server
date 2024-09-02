package com.maruhxn.todomon.domain.item.domain.item_effect;

import com.maruhxn.todomon.domain.item.dto.request.ItemEffectRequest;
import com.maruhxn.todomon.domain.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("plusCarrotEffect")
@RequiredArgsConstructor
public class PlusCarrotEffect implements ItemEffect {
    @Override
    public void applyEffect(Member member, ItemEffectRequest itemEffectRequest) {
        member.addFood(10);
    }
}
