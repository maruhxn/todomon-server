package com.maruhxn.todomon.core.domain.item.domain.item_effect;

import com.maruhxn.todomon.core.domain.item.dto.request.ItemEffectRequest;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import org.springframework.transaction.annotation.Transactional;

public interface ItemEffect {

    @Transactional
    void applyEffect(Member member, ItemEffectRequest itemEffectRequest);
}
