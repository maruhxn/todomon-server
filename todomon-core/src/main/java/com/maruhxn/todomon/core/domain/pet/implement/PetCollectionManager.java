package com.maruhxn.todomon.core.domain.pet.implement;

import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.pet.dao.CollectedPetRepository;
import com.maruhxn.todomon.core.domain.pet.domain.CollectedPet;
import com.maruhxn.todomon.core.domain.pet.domain.Pet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PetCollectionManager {

    private final CollectedPetRepository collectedPetRepository;

    public void updateCollection(Member member, Pet pet) {
        boolean isAlreadyExist = collectedPetRepository
                .existsByMember_IdAndRarityAndAppearance(member.getId(), pet.getRarity(), pet.getAppearance());
        if (!isAlreadyExist) this.appendCollection(member, pet);
    }

    private void appendCollection(Member member, Pet pet) {
        CollectedPet collectedPet = CollectedPet.of(pet);
        member.addCollection(collectedPet);
        collectedPetRepository.save(collectedPet);
    }

    public List<CollectedPet> findOwnCollection(Long memberId) {
        return collectedPetRepository.findAllByMember_Id(memberId);
    }
}
