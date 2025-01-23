package com.maruhxn.todomon.core.domain.pet.application;

import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.member.implement.MemberReader;
import com.maruhxn.todomon.core.domain.pet.domain.Pet;
import com.maruhxn.todomon.core.domain.pet.implement.PetReader;
import com.maruhxn.todomon.core.global.auth.checker.IsMyPetOrAdmin;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RepresentPetService {

    private final MemberReader memberReader;
    private final PetReader petReader;

    @IsMyPetOrAdmin
    public void setRepresentPet(Long memberId, Long petId) {
        log.info("대표 펫 설정 === 유저 아이디: {}, 펫 아이디: {}", memberId, petId);
        Member member = memberReader.findById(memberId);

        if (petId == null) {
            member.setRepresentPet(null);
            return;
        }

        Pet pet = petReader.findById(petId);
        member.setRepresentPet(pet);
    }
}
