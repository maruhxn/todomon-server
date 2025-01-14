package com.maruhxn.todomon.core.domain.pet.application;

import com.maruhxn.todomon.core.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.pet.dao.PetRepository;
import com.maruhxn.todomon.core.domain.pet.domain.Pet;
import com.maruhxn.todomon.core.domain.pet.domain.PetType;
import com.maruhxn.todomon.core.domain.pet.domain.Rarity;
import com.maruhxn.todomon.core.global.auth.model.Role;
import com.maruhxn.todomon.core.global.auth.model.provider.OAuth2Provider;
import com.maruhxn.todomon.util.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("[Service] - RepresentPetService")
class RepresentPetServiceTest extends IntegrationTestSupport {

    @Autowired
    RepresentPetService representPetService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PetRepository petRepository;

    @Test
    @DisplayName("대표 펫을 설정한다")
    void setRepresentPet() {
        // given
        Member member = createMember("tester");
        Pet pet = Pet.builder()
                .petType(PetType.getRandomPetType())
                .rarity(Rarity.COMMON)
                .build();
        member.addPet(pet);
        saveMemberToContext(member);
        petRepository.save(pet);

        // when
        representPetService.setRepresentPet(member.getId(), pet.getId());

        // then
        Optional<Pet> optionalPet = member.getRepresentPet();
        assertThat(optionalPet).isNotEmpty();
        assertThat(optionalPet.get()).isEqualTo(pet);
    }

    private Member createMember(String username) {
        Member member = Member.builder()
                .username(username)
                .email(username + "@test.com")
                .provider(OAuth2Provider.GOOGLE)
                .providerId("google_" + username)
                .role(Role.ROLE_USER)
                .profileImageUrl("profileImageUrl")
                .build();
        member.initDiligence();
        return memberRepository.save(member);
    }
}