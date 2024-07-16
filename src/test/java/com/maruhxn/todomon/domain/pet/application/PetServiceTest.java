package com.maruhxn.todomon.domain.pet.application;

import com.maruhxn.todomon.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.domain.member.domain.Member;
import com.maruhxn.todomon.domain.pet.dao.PetRepository;
import com.maruhxn.todomon.domain.pet.domain.Pet;
import com.maruhxn.todomon.domain.pet.domain.PetType;
import com.maruhxn.todomon.domain.pet.domain.Rarity;
import com.maruhxn.todomon.domain.pet.dto.request.CreatePetReq;
import com.maruhxn.todomon.global.auth.model.Role;
import com.maruhxn.todomon.global.auth.model.provider.OAuth2Provider;
import com.maruhxn.todomon.global.error.ErrorCode;
import com.maruhxn.todomon.global.error.exception.BadRequestException;
import com.maruhxn.todomon.util.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("[Service] - PetService")
class PetServiceTest extends IntegrationTestSupport {

    @Autowired
    PetService petService;

    @Autowired
    PetRepository petRepository;

    @Autowired
    MemberRepository memberRepository;

    Member member;

    @BeforeEach
    void setUp() {
        member = createMember();
    }

    @Test
    @DisplayName("랜덤으로 펫을 생성한다.")
    void createPet() {
        // given
        CreatePetReq req = CreatePetReq.builder()
                .name("테스트")
                .build();

        // when
        petService.create(member, req);

        // then
        List<Pet> pets = petRepository.findAll();
        assertThat(pets)
                .hasSize(1)
                .first()
                .extracting("name", "level", "gauge", "evolutionCnt")
                .containsExactly("테스트", 1, 0.0, 0);
    }

    @Test
    @DisplayName("펫 생성 시 펫 하우스 공간에 여유가 없다면 에러를 반환한다.")
    void createPetFailCausedByNoSpace() {
        // given
        CreatePetReq req = CreatePetReq.builder()
                .name("테스트")
                .build();
        Pet pet1 = Pet.builder()
                .petType(PetType.getRandomPetType())
                .rarity(Rarity.COMMON)
                .build();
        Pet pet2 = Pet.builder()
                .petType(PetType.getRandomPetType())
                .rarity(Rarity.COMMON)
                .build();
        Pet pet3 = Pet.builder()
                .petType(PetType.getRandomPetType())
                .rarity(Rarity.COMMON)
                .build();
        member.addPet(pet1);
        member.addPet(pet2);
        member.addPet(pet3);
        petRepository.saveAll(List.of(pet1, pet2, pet3));

        // when / then
        assertThatThrownBy(() -> petService.create(member, req))
                .hasMessage(ErrorCode.NO_SPACE_PET_HOUSE.getMessage())
                .isInstanceOf(BadRequestException.class);
    }

    private Member createMember() {
        member = Member.builder()
                .username("tester")
                .email("test@test.com")
                .provider(OAuth2Provider.GOOGLE)
                .providerId("google_foobarfoobar")
                .role(Role.ROLE_USER)
                .profileImageUrl("profileImageUrl")
                .build();
        member.initDiligence();
        return memberRepository.save(member);
    }
}