package com.maruhxn.todomon.domain.item.domain.item_effect;

import com.maruhxn.todomon.domain.item.dto.request.ItemEffectRequest;
import com.maruhxn.todomon.domain.member.domain.Member;
import org.springframework.stereotype.Service;

@Service("subscribeEffect")
public class SubscribeEffect implements ItemEffect {
    @Override
    public void applyEffect(Member member, ItemEffectRequest itemEffectRequest) {
        member.updateIsSubscribed(true);
    }
}
