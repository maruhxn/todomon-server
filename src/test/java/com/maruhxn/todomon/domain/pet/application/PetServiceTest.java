package com.maruhxn.todomon.domain.pet.application;

import com.maruhxn.todomon.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.domain.member.domain.Member;
import com.maruhxn.todomon.domain.pet.dao.PetRepository;
import com.maruhxn.todomon.domain.pet.domain.Pet;
import com.maruhxn.todomon.domain.pet.domain.PetType;
import com.maruhxn.todomon.domain.pet.domain.Rarity;
import com.maruhxn.todomon.domain.pet.dto.request.CreatePetReq;
import com.maruhxn.todomon.domain.pet.dto.request.FeedReq;
import com.maruhxn.todomon.global.auth.model.Role;
import com.maruhxn.todomon.global.auth.model.provider.OAuth2Provider;
import com.maruhxn.todomon.global.error.ErrorCode;
import com.maruhxn.todomon.global.error.exception.BadRequestException;
import com.maruhxn.todomon.util.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

import static com.maruhxn.todomon.global.common.Constants.PET_GAUGE_INCREASE_RATE;
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

        // when
        petService.create(member, null);

        // then
        List<Pet> pets = petRepository.findAll();
        assertThat(pets)
                .hasSize(1)
                .first()
                .extracting("level", "gauge", "evolutionCnt")
                .containsExactly(1, 0.0, 0);
    }

    @Test
    @DisplayName("랜덤으로 펫을 생성한다.")
    void createPetWithPaidPlan() {
        // given
        CreatePetReq req = CreatePetReq.builder()
                .name("테스트")
                .color("#000000")
                .build();
        member.updateIsSubscribed(true);
        memberRepository.save(member);

        // when
        petService.create(member, req);

        // then
        List<Pet> pets = petRepository.findAll();
        assertThat(pets)
                .hasSize(1)
                .first()
                .extracting("name", "color", "level", "gauge", "evolutionCnt")
                .containsExactly("테스트", "#000000", 1, 0.0, 0);
    }

    @Test
    @DisplayName("구독하지 않은 사용자가 펫의 이름 및 색깔을 변경하려고 할 경우, 에러를 반환한다.")
    void createPetFailByForbidden() {
        // given
        CreatePetReq req = CreatePetReq.builder()
                .name("테스트")
                .color("#000000")
                .build();

        // when / then
        assertThatThrownBy(() -> petService.create(member, req))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage(ErrorCode.NOT_SUBSCRIPTION.getMessage());

    }

    @Test
    @DisplayName("펫 생성 시 펫 하우스 공간에 여유가 없다면 에러를 반환한다.")
    void createPetFailCausedByNoSpace() {
        // given
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
        assertThatThrownBy(() -> petService.create(member, null))
                .hasMessage(ErrorCode.NO_SPACE_PET_HOUSE.getMessage())
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("펫에게 먹이를 주면, 게이지가 오르고, 유저의 보유 먹이 수는 감소한다.")
    void feed() {
        // given
        Pet pet = Pet.builder()
                .petType(PetType.getRandomPetType())
                .rarity(Rarity.COMMON)
                .build();
        member.addPet(pet);
        member.addFood(10);
        petRepository.save(pet);

        FeedReq req = new FeedReq(1);

        // when
        petService.feed(pet.getId(), member, req);

        // then
        assertThat(pet)
                .extracting("level", "gauge", "evolutionCnt")
                .containsExactly(1, PET_GAUGE_INCREASE_RATE, 0);
        assertThat(member.getFoodCnt()).isEqualTo(9);
    }

    @Test
    @DisplayName("펫에게 먹이를 주어 게이지가 오를 때, 100%를 초과하면 레벨업을 한다.")
    void feedWithLevelUp() {
        // given
        Pet pet = Pet.builder()
                .petType(PetType.getRandomPetType())
                .rarity(Rarity.COMMON)
                .build();
        pet.increaseGauge(99.0);
        member.addPet(pet);
        member.addFood(10);
        petRepository.save(pet);

        FeedReq req = new FeedReq(1);

        // when
        petService.feed(pet.getId(), member, req);

        // then
        assertThat(pet)
                .extracting("level", "gauge", "evolutionCnt")
                .containsExactly(2, (99.0 + PET_GAUGE_INCREASE_RATE) % 100, 0);
        assertThat(member.getFoodCnt()).isEqualTo(9);
    }

    @Test
    @DisplayName("펫에게 먹이를 주어 게이지가 오를 때, 레벨업을 하여 30Lv이 넘을 경우, 진화한다.")
    void feedWithEvolution() {
        // given
        Pet pet = Pet.builder()
                .petType(PetType.getRandomPetType())
                .rarity(Rarity.COMMON)
                .build();
        String prevAppearance = pet.getAppearance();
        for (int i = 0; i < 28; i++) {
            pet.levelUp();
        }

        pet.increaseGauge(80.0);
        member.addPet(pet);
        member.addFood(10);
        petRepository.save(pet);

        FeedReq req = new FeedReq(10);

        // when
        petService.feed(pet.getId(), member, req);

        // then
        assertThat(prevAppearance).isNotEqualTo(pet.getAppearance());
        assertThat(pet)
                .extracting("level", "gauge", "evolutionCnt")
                .containsExactly(30, 0.0, 1);
    }

    @Test
    @DisplayName("펫에게 먹이를 줄 때, 요청된 먹이 수가 보유한 먹이 수보다 많을 경우 에러가 발생한다.")
    void feedFailByOverFood() {
        // given
        Pet pet = Pet.builder()
                .petType(PetType.getRandomPetType())
                .rarity(Rarity.COMMON)
                .build();
        member.addPet(pet);
        member.addFood(1);
        petRepository.save(pet);

        FeedReq req = new FeedReq(10);

        // when
        assertThatThrownBy(() -> petService.feed(pet.getId(), member, req))
                .hasMessage(ErrorCode.OVER_FOOD_CNT.getMessage())
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