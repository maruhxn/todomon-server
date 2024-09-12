package com.maruhxn.todomon.core.domain.pet.application;

import com.maruhxn.todomon.core.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.pet.dao.CollectedPetRepository;
import com.maruhxn.todomon.core.domain.pet.dao.PetRepository;
import com.maruhxn.todomon.core.domain.pet.domain.CollectedPet;
import com.maruhxn.todomon.core.domain.pet.domain.Pet;
import com.maruhxn.todomon.core.domain.pet.dto.request.ChangePetNameRequest;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.BadRequestException;
import com.maruhxn.todomon.core.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.maruhxn.todomon.core.global.common.Constants.PET_GAUGE_INCREASE_RATE;

@Service
@Transactional
@RequiredArgsConstructor
public class PetService {
    private final MemberRepository memberRepository;
    private final PetRepository petRepository;
    private final CollectedPetRepository collectedPetRepository;

    public void create(Long memberId) {
        Member member = memberRepository.findMemberWithPets(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_MEMBER));
        validatePetHouseSpace(member);

        // 펫 랜덤 생성
        Pet pet = Pet.getRandomPet();
        member.addPet(pet);
        petRepository.save(pet);

        // 펫 도감 등록
        updatePetCollection(member, pet);
    }

    public void updatePetName(Long memberId, ChangePetNameRequest req) {
        Pet findPet = petRepository.findOneByIdAndMember_Id(req.getPetId(), memberId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_PET));
        findPet.changeName(req);
    }

    private void updatePetCollection(Member member, Pet pet) {
        collectedPetRepository
                .findByMember_IdAndRarityAndAppearance(member.getId(), pet.getRarity(), pet.getAppearance())
                .ifPresentOrElse(
                        findPet -> {
                            /* 아무 작업도 수행 X */
                        },
                        () -> {
                            CollectedPet collectedPet = CollectedPet.of(pet);
                            member.addCollection(collectedPet);
                            collectedPetRepository.save(collectedPet);
                        }
                );
    }

    private void validatePetHouseSpace(Member member) {
        if (member.getPetHouseSize() <= member.getPets().size()) {
            throw new BadRequestException(ErrorCode.NO_SPACE_PET_HOUSE);
        }
    }

    public void feed(Long petId, Member member, Long foodCnt) {
        Pet findPet = petRepository.findOneByIdAndMember_Id(petId, member.getId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_PET));
        int prevEvolutionCnt = findPet.getEvolutionCnt();
        if (foodCnt > member.getFoodCnt()) {
            throw new BadRequestException(ErrorCode.OVER_FOOD_CNT);
        }

        // 요청 먹이 수만큼 펫 게이지 올리기
        findPet.increaseGauge(foodCnt * PET_GAUGE_INCREASE_RATE);
        if (prevEvolutionCnt != findPet.getEvolutionCnt()) updatePetCollection(member, findPet);

        // 멤버의 소지 먹이 수 감소
        member.decreaseFoodCnt(foodCnt);
    }

    public void deletePet(Long memberId, Long petId) {
        Pet findPet = petRepository.findById(petId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_PET));

        Member findMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_MEMBER));

        findMember.getRepresentPet().ifPresent(representPet -> { // 삭제하려는 펫이 대표 펫이었을 경우, 대표펫을 null로 설정
            if (representPet.getId().equals(petId)) {
                findMember.setRepresentPet(null);
            }
        });

        petRepository.delete(findPet);
    }
}
