package com.maruhxn.todomon.core.domain.pet.application;

import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.member.implement.MemberReader;
import com.maruhxn.todomon.core.domain.pet.domain.Pet;
import com.maruhxn.todomon.core.domain.pet.dto.request.ChangePetNameReq;
import com.maruhxn.todomon.core.domain.pet.implement.PetCollectionManager;
import com.maruhxn.todomon.core.domain.pet.implement.PetReader;
import com.maruhxn.todomon.core.domain.pet.implement.PetWriter;
import com.maruhxn.todomon.core.global.auth.checker.IsMyPetOrAdmin;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import static com.maruhxn.todomon.core.global.common.Constants.PET_GAUGE_INCREASE_RATE;

@Service
@Transactional
@RequiredArgsConstructor
public class PetService {

    private final MemberReader memberReader;
    private final PetReader petReader;
    private final PetWriter petWriter;
    private final PetCollectionManager petCollectionManager;

    public void create(Long memberId) {
        Member member = memberReader.findMemberWithPetsById(memberId);
        member.validatePetHouseSpace();
        Pet pet = petWriter.createRandomPet(member);
        petCollectionManager.updateCollection(member, pet);
    }

    public void updatePetNameAndColor(Long memberId, ChangePetNameReq req) {
        Pet pet = petReader.findByIdAndMemberId(req.getPetId(), memberId);
        pet.changeName(req.getName());
        if (StringUtils.hasText(req.getColor())) pet.updateColor(req.getColor());
    }

    @IsMyPetOrAdmin
    public void feed(Long memberId, Long petId, Long foodCnt) {
        Member member = memberReader.findById(memberId);
        Pet pet = petReader.findById(petId);
        member.validateFoodCnt(foodCnt);
        // 요청 먹이 수만큼 펫 게이지 올리기
        this.increaseGaugeAndCheckEvolution(foodCnt, pet, member);
        // 멤버의 소지 먹이 수 감소
        member.decreaseFoodCnt(foodCnt);
    }

    // TODO: evolutionGap이 1보다 클 경우, 모두 도감에 등록하도록 수정해야 함..
    private void increaseGaugeAndCheckEvolution(Long foodCnt, Pet pet, Member member) {
        int evolutionGap = pet.increaseGaugeAndGetEvolutionGap(foodCnt * PET_GAUGE_INCREASE_RATE);
        if (evolutionGap > 0) petCollectionManager.updateCollection(member, pet);
    }

    @IsMyPetOrAdmin
    public void deletePet(Long memberId, Long petId) {
        Pet pet = petReader.findById(petId);
        Member member = memberReader.findMemberWithRepresentPet(memberId);
        petWriter.remove(pet);
        this.checkIsDeletedRepresentPet(petId, member);
    }

    private void checkIsDeletedRepresentPet(Long petId, Member member) {
        member.getRepresentPet()
                .ifPresent(representPet -> { // 삭제하려는 펫이 대표 펫이었을 경우, 대표펫을 null로 설정
                            if (representPet.getId().equals(petId)) {
                                member.setRepresentPet(null);
                            }
                        }
                );
    }
}
