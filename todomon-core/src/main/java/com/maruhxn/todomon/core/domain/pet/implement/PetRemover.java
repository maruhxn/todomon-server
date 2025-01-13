package com.maruhxn.todomon.core.domain.pet.implement;

import com.maruhxn.todomon.core.domain.pet.dao.PetRepository;
import com.maruhxn.todomon.core.domain.pet.domain.Pet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PetRemover {

    private final PetRepository petRepository;

    public void remove(Pet pet) {
        petRepository.delete(pet);
    }

}
