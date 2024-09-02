package com.maruhxn.todomon.domain.item.domain.item_effect;

import com.maruhxn.todomon.domain.item.dto.request.ItemEffectRequest;
import com.maruhxn.todomon.domain.member.domain.Member;
import com.maruhxn.todomon.domain.pet.application.PetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("petSummonEffect")
@RequiredArgsConstructor
public class PetSummonEffect implements ItemEffect {

    private final PetService petService;

    @Override
    public void applyEffect(Member member, ItemEffectRequest itemEffectRequest) {
        petService.create(member.getId());
    }
}
