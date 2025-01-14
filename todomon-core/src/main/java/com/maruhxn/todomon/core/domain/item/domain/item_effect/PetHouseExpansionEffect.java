package com.maruhxn.todomon.core.domain.item.domain.item_effect;

import com.maruhxn.todomon.core.domain.item.dto.request.ItemEffectReq;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.BadRequestException;
import org.springframework.stereotype.Service;

import static com.maruhxn.todomon.core.global.common.Constants.MAX_PET_HOUSE_SIZE;

@Service("petHouseExpansionEffect")
public class PetHouseExpansionEffect implements ItemEffect {
    @Override
    public void applyEffect(Member member, ItemEffectReq itemEffectReq) {
        if (isOverMaxSize(member)) {
            throw new BadRequestException(
                    ErrorCode.BAD_REQUEST,
                    String.format("펫 하우스는 %d칸을 초과할 수 없습니다.", MAX_PET_HOUSE_SIZE)
            );
        }

        member.expandPetHouse();
    }

    private static boolean isOverMaxSize(Member member) {
        return member.getPetHouseSize() >= MAX_PET_HOUSE_SIZE;
    }
}
