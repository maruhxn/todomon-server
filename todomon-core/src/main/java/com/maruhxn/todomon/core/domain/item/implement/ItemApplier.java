package com.maruhxn.todomon.core.domain.item.implement;

import com.maruhxn.todomon.core.domain.item.domain.Item;
import com.maruhxn.todomon.core.domain.item.domain.item_effect.ItemEffect;
import com.maruhxn.todomon.core.domain.item.dto.request.ItemEffectRequest;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ItemApplier {

    private final ApplicationContext applicationContext;

    public void apply(Item item, Member member, ItemEffectRequest req) {
        String effectName = item.getEffectName();
        ItemEffect itemEffect = (ItemEffect) applicationContext.getBean(effectName);
        itemEffect.applyEffect(member, req);
    }
}
