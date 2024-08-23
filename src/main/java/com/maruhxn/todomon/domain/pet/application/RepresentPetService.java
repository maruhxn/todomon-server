package com.maruhxn.todomon.domain.pet.application;

import com.maruhxn.todomon.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.domain.member.domain.Member;
import com.maruhxn.todomon.domain.pet.dao.PetRepository;
import com.maruhxn.todomon.domain.pet.domain.Pet;
import com.maruhxn.todomon.global.error.ErrorCode;
import com.maruhxn.todomon.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RepresentPetService {

    private final MemberRepository memberRepository;
    private final PetRepository petRepository;

    public void setRepresentPet(Long memberId, Long petId) {
        Member findMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_MEMBER));

        if (petId == null) {
            findMember.setRepresentPet(null);
        } else {
            Pet pet = petRepository.findOneByIdAndMember_Id(petId, memberId)
                    .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_PET));

            findMember.setRepresentPet(pet);
        }
    }
}
