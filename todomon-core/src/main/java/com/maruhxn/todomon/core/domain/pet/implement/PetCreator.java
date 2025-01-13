package com.maruhxn.todomon.core.domain.pet.implement;

import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.pet.dao.PetRepository;
import com.maruhxn.todomon.core.domain.pet.domain.Pet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PetCreator {

    private final PetRepository petRepository;

    public Pet createRandomPet(Member member) {
        // 펫 랜덤 생성
        Pet pet = Pet.getRandomPet();
        member.addPet(pet);
        petRepository.save(pet);

        return pet;
    }
}
