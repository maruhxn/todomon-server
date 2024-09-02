package com.maruhxn.todomon.domain.item.domain.item_effect;

import com.maruhxn.todomon.domain.item.dto.request.ItemEffectRequest;
import com.maruhxn.todomon.domain.member.domain.Member;
import org.springframework.transaction.annotation.Transactional;

public interface ItemEffect {

    @Transactional
    void applyEffect(Member member, ItemEffectRequest itemEffectRequest);
}
