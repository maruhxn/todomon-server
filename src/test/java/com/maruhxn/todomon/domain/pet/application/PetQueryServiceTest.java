package com.maruhxn.todomon.domain.pet.application;

import com.maruhxn.todomon.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.domain.member.domain.Member;
import com.maruhxn.todomon.domain.pet.dao.CollectedPetRepository;
import com.maruhxn.todomon.domain.pet.dao.PetRepository;
import com.maruhxn.todomon.domain.pet.domain.CollectedPet;
import com.maruhxn.todomon.domain.pet.domain.Pet;
import com.maruhxn.todomon.domain.pet.domain.PetType;
import com.maruhxn.todomon.domain.pet.domain.Rarity;
import com.maruhxn.todomon.domain.pet.dto.response.PetDexItem;
import com.maruhxn.todomon.global.auth.model.Role;
import com.maruhxn.todomon.global.auth.model.provider.OAuth2Provider;
import com.maruhxn.todomon.util.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("[Service] - PetQueryService")
class PetQueryServiceTest extends IntegrationTestSupport {

    @Autowired
    PetQueryService petQueryService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PetRepository petRepository;

    @Autowired
    CollectedPetRepository collectedPetRepository;

    Member member;

    @BeforeEach
    void setUp() {
        member = createMember();
    }

    @Test
    void findAllPetTypes() {
        // when
        List<PetDexItem> allPetTypes = petQueryService.findAllPetTypes();

        // then
        assertThat(allPetTypes).hasSize(54);
    }

    @Test
    void findAllMyCollectedPets() {
        // given
        Pet pet1 = Pet.builder()
                .petType(PetType.DOG)
                .rarity(Rarity.COMMON)
                .build();
        Pet pet2 = Pet.builder()
                .petType(PetType.CAT)
                .rarity(Rarity.COMMON)
                .build();
        member.addPet(pet1);
        member.addPet(pet2);
        petRepository.saveAll(List.of(pet1, pet2));

        CollectedPet collectedPet1 = CollectedPet.builder()
                .rarity(pet1.getRarity())
                .evolutionCnt(pet1.getEvolutionCnt())
                .petType(pet1.getPetType())
                .build();
        collectedPet1.setMember(member);

        CollectedPet collectedPet2 = CollectedPet.builder()
                .rarity(pet2.getRarity())
                .evolutionCnt(pet2.getEvolutionCnt())
                .petType(pet2.getPetType())
                .build();
        collectedPet2.setMember(member);
        collectedPetRepository.saveAll(List.of(collectedPet1, collectedPet2));

        // when
        List<PetDexItem> allPetTypes = petQueryService.findAllPetTypes();
        List<PetDexItem> allMyCollectedPets = petQueryService.findAllMyCollectedPets(member);

        // then
        List<PetDexItem> commonElements = allPetTypes.stream()
                .filter(allMyCollectedPets::contains)
                .toList();
        assertThat(commonElements).hasSize(2);
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