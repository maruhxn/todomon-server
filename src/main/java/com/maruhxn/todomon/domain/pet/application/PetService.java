package com.maruhxn.todomon.domain.pet.application;

import com.maruhxn.todomon.domain.member.domain.Member;
import com.maruhxn.todomon.domain.pet.dao.PetRepository;
import com.maruhxn.todomon.domain.pet.domain.Pet;
import com.maruhxn.todomon.domain.pet.domain.PetType;
import com.maruhxn.todomon.domain.pet.domain.Rarity;
import com.maruhxn.todomon.domain.pet.dto.request.CreatePetReq;
import com.maruhxn.todomon.domain.pet.dto.request.FeedReq;
import com.maruhxn.todomon.global.error.ErrorCode;
import com.maruhxn.todomon.global.error.exception.BadRequestException;
import com.maruhxn.todomon.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.maruhxn.todomon.global.common.Constants.PET_GAUGE_INCREASE_RATE;

@Service
@Transactional
@RequiredArgsConstructor
public class PetService {
    private final PetRepository petRepository;

    public void create(Member member, CreatePetReq req) {
        if (member.getPetHouseSize() <= member.getPets().size())
            throw new BadRequestException(ErrorCode.NO_SPACE_PET_HOUSE);
        // 펫 랜덤 생성
        Pet pet = Pet.builder()
                .name(req.getName())
                .rarity(Rarity.getRandomRarity()) // 랜덤
                .petType(PetType.getRandomPetType()) // 랜덤
                .build();
        member.addPet(pet);
        petRepository.save(pet);
    }


    public void feed(Long petId, Member member, FeedReq req) {
        Pet findPet = petRepository.findById(petId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_PET));

        if (req.getFoodCnt() > member.getFoodCnt()) {
            throw new BadRequestException(ErrorCode.OVER_FOOD_CNT);
        }

        // 요청 먹이 수만큼 펫 게이지 올리기
        findPet.increaseGauge(req.getFoodCnt() * PET_GAUGE_INCREASE_RATE);

        // 멤버의 소지 먹이 수 감소
        member.decreaseFoodCnt(req.getFoodCnt());
    }
}
