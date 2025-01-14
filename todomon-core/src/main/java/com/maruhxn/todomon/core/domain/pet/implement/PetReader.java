package com.maruhxn.todomon.core.domain.pet.implement;

import com.maruhxn.todomon.core.domain.pet.dao.PetRepository;
import com.maruhxn.todomon.core.domain.pet.domain.Pet;
import com.maruhxn.todomon.core.domain.pet.domain.PetType;
import com.maruhxn.todomon.core.domain.pet.domain.Rarity;
import com.maruhxn.todomon.core.domain.pet.dto.response.PetDexItem;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PetReader {

    private final PetRepository petRepository;

    public List<PetDexItem> findAllKindsOfPets() {
        List<PetDexItem> dex = new ArrayList<>();

        for (PetType petType : PetType.values()) {
            for (int i = 0; i <= petType.getEvolutionaryCnt(); i++) {
                PetType.EvolutionStage stage = petType.getEvolutionStage(i);
                for (Rarity rarity : Rarity.values()) {
                    dex.add(PetDexItem.of(stage, rarity));
                }
            }
        }

        return dex;
    }

    public Pet findById(Long id) {
        return petRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_PET));
    }

    public Pet findByIdAndMemberId(Long id, Long memberId) {
        return petRepository.findOneByIdAndMember_Id(id, memberId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_PET));
    }

    public List<Pet> findAllByMemberId(Long memberId) {
        return petRepository.findAllByMember_Id(memberId);
    }
}
