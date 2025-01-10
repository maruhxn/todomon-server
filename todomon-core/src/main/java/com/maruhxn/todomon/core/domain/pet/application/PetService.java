package com.maruhxn.todomon.core.domain.pet.application;

import com.maruhxn.todomon.core.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.pet.dao.CollectedPetRepository;
import com.maruhxn.todomon.core.domain.pet.dao.PetRepository;
import com.maruhxn.todomon.core.domain.pet.domain.CollectedPet;
import com.maruhxn.todomon.core.domain.pet.domain.Pet;
import com.maruhxn.todomon.core.domain.pet.dto.request.ChangePetNameReq;
import com.maruhxn.todomon.core.global.auth.checker.IsMyPetOrAdmin;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.BadRequestException;
import com.maruhxn.todomon.core.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;

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
        this.validatePetHouseSpace(member);

        // 펫 랜덤 생성
        Pet pet = Pet.getRandomPet();
        member.addPet(pet);
        petRepository.save(pet);

        // 펫 도감 등록
        this.updatePetCollection(member, pet);
    }

    private void validatePetHouseSpace(Member member) {
        if (member.getPetHouseSize() <= member.getPets().size()) {
            throw new BadRequestException(ErrorCode.NO_SPACE_PET_HOUSE);
        }
    }

    private void updatePetCollection(Member member, Pet pet) {
        Optional<CollectedPet> optionalCollectedPet = collectedPetRepository
                .findByMember_IdAndRarityAndAppearance(member.getId(), pet.getRarity(), pet.getAppearance());

        if (optionalCollectedPet.isPresent()) return; // 이미 있다면 아무 작업 수행 X

        CollectedPet collectedPet = CollectedPet.of(pet);
        member.addCollection(collectedPet);
        collectedPetRepository.save(collectedPet);
    }

    public void updatePetNameAndColor(Long memberId, ChangePetNameReq req) {
        Pet findPet = petRepository.findOneByIdAndMember_Id(req.getPetId(), memberId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_PET));

        findPet.changeName(req.getName());
        if (StringUtils.hasText(req.getColor())) findPet.updateColor(req.getColor());
    }

    @IsMyPetOrAdmin
    public void feed(Long memberId, Long petId, Long foodCnt) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_MEMBER));

        Pet findPet = petRepository.findById(petId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_PET));

        if (member.isInvalidFoodCnt(foodCnt)) {
            throw new BadRequestException(ErrorCode.OVER_FOOD_CNT);
        }

        // 요청 먹이 수만큼 펫 게이지 올리기
        this.increaseGaugeAndCheckEvolution(foodCnt, findPet, member);

        // 멤버의 소지 먹이 수 감소
        member.decreaseFoodCnt(foodCnt);
    }

    // TODO: evolutionGap이 1보다 클 경우, 모두 도감에 등록하도록 수정해야 함..
    private void increaseGaugeAndCheckEvolution(Long foodCnt, Pet findPet, Member member) {
        int evolutionGap = findPet.increaseGaugeAndGetEvolutionGap(foodCnt * PET_GAUGE_INCREASE_RATE);
        if (evolutionGap > 0) this.updatePetCollection(member, findPet);
    }

    @IsMyPetOrAdmin
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
