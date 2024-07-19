package com.maruhxn.todomon.domain.pet.application;

import com.maruhxn.todomon.domain.member.domain.Member;
import com.maruhxn.todomon.domain.pet.dao.CollectedPetRepository;
import com.maruhxn.todomon.domain.pet.dao.PetRepository;
import com.maruhxn.todomon.domain.pet.domain.CollectedPet;
import com.maruhxn.todomon.domain.pet.domain.Pet;
import com.maruhxn.todomon.domain.pet.domain.PetType;
import com.maruhxn.todomon.domain.pet.domain.Rarity;
import com.maruhxn.todomon.domain.pet.dto.request.CreatePetReq;
import com.maruhxn.todomon.domain.pet.dto.request.FeedReq;
import com.maruhxn.todomon.global.error.ErrorCode;
import com.maruhxn.todomon.global.error.exception.BadRequestException;
import com.maruhxn.todomon.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.maruhxn.todomon.global.common.Constants.PET_GAUGE_INCREASE_RATE;

@Service
@Transactional
@RequiredArgsConstructor
public class PetService {
    private final PetRepository petRepository;
    private final CollectedPetRepository collectedPetRepository;

    public void create(Member member, CreatePetReq req) {
        validateMemberSubscription(member, req);
        validatePetHouseSpace(member);

        // 펫 랜덤 생성
        Pet pet = createPet(member, req);
        member.addPet(pet);
        petRepository.save(pet);

        // 펫 도감 등록
        updatePetCollection(member, pet);
    }

    private void updatePetCollection(Member member, Pet pet) {
        collectedPetRepository
                .findByMember_IdAndRarityAndAppearance(member.getId(), pet.getRarity(), pet.getAppearance())
                .ifPresentOrElse(
                        findPet -> {
                            /* 아무 작업도 수행 X */
                        },
                        () -> {
                            CollectedPet collectedPet = CollectedPet.builder()
                                    .rarity(pet.getRarity())
                                    .petType(pet.getPetType())
                                    .evolutionCnt(pet.getEvolutionCnt())
                                    .build();
                            collectedPet.setMember(member);
                            collectedPetRepository.save(collectedPet);
                        }
                );
    }

    private void validateMemberSubscription(Member member, CreatePetReq req) {
        if (!member.isSubscribed() && req != null) {
            throw new AccessDeniedException(ErrorCode.NOT_SUBSCRIPTION.getMessage());
        }
    }

    private void validatePetHouseSpace(Member member) {
        if (member.getPetHouseSize() <= member.getPets().size()) {
            throw new BadRequestException(ErrorCode.NO_SPACE_PET_HOUSE);
        }
    }

    private Pet createPet(Member member, CreatePetReq req) {
        Pet.PetBuilder petBuilder = Pet.builder()
                .rarity(Rarity.getRandomRarity()) // 랜덤
                .petType(PetType.getRandomPetType()); // 랜덤

        if (member.isSubscribed() && req != null) {
            petBuilder
                    .name(req.getName())
                    .color(req.getColor());
        }

        return petBuilder.build();
    }


    public void feed(Long petId, Member member, FeedReq req) {
        Pet findPet = petRepository.findById(petId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_PET));
        int prevEvolutionCnt = findPet.getEvolutionCnt();
        if (req.getFoodCnt() > member.getFoodCnt()) {
            throw new BadRequestException(ErrorCode.OVER_FOOD_CNT);
        }

        // 요청 먹이 수만큼 펫 게이지 올리기
        findPet.increaseGauge(req.getFoodCnt() * PET_GAUGE_INCREASE_RATE);
        if (prevEvolutionCnt != findPet.getEvolutionCnt()) updatePetCollection(member, findPet);

        // 멤버의 소지 먹이 수 감소
        member.decreaseFoodCnt(req.getFoodCnt());
    }
}
