package com.maruhxn.todomon.core.domain.pet.application;

import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.member.implement.MemberReader;
import com.maruhxn.todomon.core.domain.pet.domain.Pet;
import com.maruhxn.todomon.core.domain.pet.dto.response.OwnPetInfoRes;
import com.maruhxn.todomon.core.domain.pet.dto.response.PetDexItem;
import com.maruhxn.todomon.core.domain.pet.implement.PetCollectionManager;
import com.maruhxn.todomon.core.domain.pet.implement.PetReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
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
        log.debug("유저 소유 펫 조회 === 유저 아이디: {}", memberId);
        Member member = memberReader.findById(memberId);
        List<Pet> pets = petReader.findAllByMemberId(memberId);
        return OwnPetInfoRes.of(member, pets);
    }

    public List<PetDexItem> findAllOwnCollectedPets(Long memberId) {
        log.debug("유저 펫 도감 조회 === 유저 아이디: {}", memberId);
        return collectionManager.findOwnCollection(memberId)
                .stream().map(PetDexItem::from).toList();
    }
}
