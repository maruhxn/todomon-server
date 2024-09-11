package com.maruhxn.todomon.core.domain.item.domain.item_effect;

import com.maruhxn.todomon.core.domain.item.dto.request.ItemEffectRequest;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.pet.application.PetService;
import com.maruhxn.todomon.core.domain.pet.dto.request.ChangePetNameRequest;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("changePetNameEffect")
@RequiredArgsConstructor
public class ChangePetNameEffect implements ItemEffect {

    private final PetService petService;

    @Override
    public void applyEffect(Member member, ItemEffectRequest request) {
        if (!(request instanceof ChangePetNameRequest changePetNameRequest)) {
            throw new BadRequestException(ErrorCode.BAD_REQUEST, "잘못된 요청 타입입니다.");
        }

        petService.updatePetName(member.getId(), changePetNameRequest);
    }
}
