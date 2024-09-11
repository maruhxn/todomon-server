package com.maruhxn.todomon.core.domain.item.domain.item_effect;

import com.maruhxn.todomon.core.domain.item.dto.request.ItemEffectRequest;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import org.springframework.stereotype.Service;

@Service("subscribeEffect")
public class SubscribeEffect implements ItemEffect {
    @Override
    public void applyEffect(Member member, ItemEffectRequest itemEffectRequest) {
        member.updateIsSubscribed(true);
    }
}
