package com.maruhxn.todomon.domain.pet.application;

import com.maruhxn.todomon.domain.member.domain.Member;
import com.maruhxn.todomon.domain.pet.dao.CollectedPetRepository;
import com.maruhxn.todomon.domain.pet.dao.PetRepository;
import com.maruhxn.todomon.domain.pet.domain.CollectedPet;
import com.maruhxn.todomon.domain.pet.domain.Pet;
import com.maruhxn.todomon.domain.pet.domain.PetType;
import com.maruhxn.todomon.domain.pet.domain.Rarity;
import com.maruhxn.todomon.domain.pet.dto.response.PetDexItem;
import com.maruhxn.todomon.domain.pet.dto.response.PetItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PetQueryService {

    private final PetRepository petRepository;
    private final CollectedPetRepository collectedPetRepository;

    public List<PetDexItem> findAllPetTypes() {
        List<PetDexItem> dex = new ArrayList<>();
        for (PetType petType : PetType.values()) {
            for (int i = 0; i <= petType.getEvolutionaryCnt(); i++) {
                PetType.EvolutionStage stage = petType.getEvolutionStage(i);
                for (Rarity rarity : Rarity.values()) {
                    dex.add(PetDexItem.builder()
                            .name(stage.getName())
                            .appearance(stage.getForm())
                            .rarity(rarity)
                            .build());
                }
            }
        }

        return dex.stream().distinct().toList();
    }

    public List<PetItem> findAllMyPets(Member member) {
        List<Pet> pets = petRepository.findAllByMember_Id(member.getId());
        return pets.stream().map(PetItem::from).toList();
    }

    public List<PetDexItem> findAllMyCollectedPets(Member member) {
        List<CollectedPet> collections = collectedPetRepository.findAllByMember_Id(member.getId());
        return collections.stream().map(PetDexItem::from).toList();
    }
}
