package com.maruhxn.todomon.core.domain.pet.application;

import com.maruhxn.todomon.core.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.pet.dao.CollectedPetRepository;
import com.maruhxn.todomon.core.domain.pet.dao.PetRepository;
import com.maruhxn.todomon.core.domain.pet.domain.CollectedPet;
import com.maruhxn.todomon.core.domain.pet.domain.Pet;
import com.maruhxn.todomon.core.domain.pet.domain.PetType;
import com.maruhxn.todomon.core.domain.pet.domain.Rarity;
import com.maruhxn.todomon.core.domain.pet.dto.response.PetDexItem;
import com.maruhxn.todomon.core.domain.pet.dto.response.PetInfoDto;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PetQueryService {

    private final MemberRepository memberRepository;
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

    public PetInfoDto findAllOwnPets(Long memberId) {
        Member findMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_MEMBER));
        List<Pet> pets = petRepository.findAllByMember_Id(memberId);
        return PetInfoDto.of(findMember, pets);
    }

    public List<PetDexItem> findAllOwnCollectedPets(Long memberId) {
        List<CollectedPet> collections = collectedPetRepository.findAllByMember_Id(memberId);
        return collections.stream().map(PetDexItem::from).toList();
    }
}
