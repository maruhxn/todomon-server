package com.maruhxn.todomon.core.domain.pet.application;

import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.member.implement.MemberReader;
import com.maruhxn.todomon.core.domain.pet.domain.Pet;
import com.maruhxn.todomon.core.domain.pet.dto.response.OwnPetInfoRes;
import com.maruhxn.todomon.core.domain.pet.dto.response.PetDexItem;
import com.maruhxn.todomon.core.domain.pet.implement.PetCollectionManager;
import com.maruhxn.todomon.core.domain.pet.implement.PetReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PetQueryService {

    private final MemberReader memberReader;
    private final PetReader petReader;
    private final PetCollectionManager collectionManager;

    public List<PetDexItem> findAllPetTypes() {
        return petReader.findAllKindsOfPets().stream()
                .distinct().toList();
    }

    public OwnPetInfoRes findAllOwnPets(Long memberId) {
        Member member = memberReader.findById(memberId);
        List<Pet> pets = petReader.findAllByMemberId(memberId);
        return OwnPetInfoRes.of(member, pets);
    }

    public List<PetDexItem> findAllOwnCollectedPets(Long memberId) {
        return collectionManager.findOwnCollection(memberId)
                .stream().map(PetDexItem::from).toList();
    }
}
