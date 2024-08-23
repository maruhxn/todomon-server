package com.maruhxn.todomon.domain.pet.api;

import com.maruhxn.todomon.domain.member.domain.Member;
import com.maruhxn.todomon.domain.pet.dao.CollectedPetRepository;
import com.maruhxn.todomon.domain.pet.dao.PetRepository;
import com.maruhxn.todomon.domain.pet.domain.CollectedPet;
import com.maruhxn.todomon.domain.pet.domain.Pet;
import com.maruhxn.todomon.domain.pet.domain.PetType;
import com.maruhxn.todomon.domain.pet.domain.Rarity;
import com.maruhxn.todomon.domain.pet.dto.request.FeedReq;
import com.maruhxn.todomon.global.auth.model.Role;
import com.maruhxn.todomon.global.auth.model.provider.OAuth2Provider;
import com.maruhxn.todomon.global.error.ErrorCode;
import com.maruhxn.todomon.util.ControllerIntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.List;

import static com.maruhxn.todomon.global.auth.application.JwtProvider.BEARER_PREFIX;
import static com.maruhxn.todomon.global.common.Constants.ACCESS_TOKEN_HEADER;
import static com.maruhxn.todomon.global.common.Constants.REFRESH_TOKEN_HEADER;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("[Integration] - Pet")
class PetIntegrationTest extends ControllerIntegrationTestSupport {

    static final String PET_BASE_URL = "/api/pets";
    static final String MY_PET_BASE_URL = "/api/pets/my";
    static final String MEMBER_PET_BASE_URL = "/api/members/{memberId}/pets";

    @Autowired
    PetRepository petRepository;

    @Autowired
    CollectedPetRepository collectedPetRepository;


    @Test
    @DisplayName("GET /api/pets 요청 시 모든 종류 펫을 조회한다.")
    void getAllPetTypes() throws Exception {
        // given

        // when / then
        mockMvc.perform(
                        get(PET_BASE_URL)
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("OK"))
                .andExpect(jsonPath("message").value("모든 종류 펫 조회 성공"))
                .andExpect(jsonPath("data").isArray())
                .andExpect(jsonPath("data.size()").value(54));
    }

    @Test
    @DisplayName("GET /api/members/{memberId}/pets - 유저가 소유하고 있는 모든 펫을 조회한다.")
    void getPetInfo() throws Exception {
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

        // when / then
        mockMvc.perform(
                        get(MEMBER_PET_BASE_URL, member.getId())
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("OK"))
                .andExpect(jsonPath("message").value("소유 펫 정보 조회 성공"))
                .andExpect(jsonPath("data.representPetId").isEmpty())
                .andExpect(jsonPath("data.starPoint").value(member.getStarPoint()))
                .andExpect(jsonPath("data.foodCnt").value(member.getFoodCnt()))
                .andExpect(jsonPath("data.petHouseSize").value(member.getPetHouseSize()))
                .andExpect(jsonPath("data.myPets").isArray())
                .andExpect(jsonPath("data.myPets.size()").value(2))
                .andExpect(jsonPath("data.myPets[0].id").value(pet1.getId()))
                .andExpect(jsonPath("data.myPets[0].name").value(pet1.getName()))
                .andExpect(jsonPath("data.myPets[0].rarity").value(pet1.getRarity().name()))
                .andExpect(jsonPath("data.myPets[0].appearance").value(pet1.getAppearance()))
                .andExpect(jsonPath("data.myPets[0].color").value(pet1.getColor()))
                .andExpect(jsonPath("data.myPets[0].level").value(pet1.getLevel()))
                .andExpect(jsonPath("data.myPets[0].gauge").value(pet1.getGauge()))
                .andExpect(jsonPath("data.myPets[0].petType").value(pet1.getPetType().name()));

    }

    @Test
    @DisplayName("GET /api/members/{memberId}/pets/collections - 유저가 획득한 펫 목록을 조회한다.")
    void getPetCollections() throws Exception {
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

        // when / then
        mockMvc.perform(
                        get(MEMBER_PET_BASE_URL + "/collections", member.getId())
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("OK"))
                .andExpect(jsonPath("message").value("펫 획득 목록 조회 성공"))
                .andExpect(jsonPath("data").isArray())
                .andExpect(jsonPath("data.size()").value(2))
                .andExpect(jsonPath("data[0].rarity").value(Rarity.COMMON.name()))
                .andExpect(jsonPath("data[0].name").value(pet1.getName()))
                .andExpect(jsonPath("data[0].appearance").value(PetType.DOG.getEvolutionStage(0).getForm()))
                .andExpect(jsonPath("data[0].color").value(Rarity.COMMON.getColor()));
    }

    @Test
    @DisplayName("PATCH /api/pets/my/represent-pet?petId=#petId - 해당 펫을 대표 펫으로 설정한다.")
    void updateRepresentPet() throws Exception {
        // given
        Pet pet = Pet.builder()
                .petType(PetType.getRandomPetType())
                .rarity(Rarity.COMMON)
                .build();
        member.addPet(pet);
        petRepository.save(pet);

        // when / then
        mockMvc.perform(
                        patch(MY_PET_BASE_URL + "/represent-pet", member.getId())
                                .queryParam("petId", String.valueOf(pet.getId()))
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PATCH /api/pets/my/represent-pet?petId=#petId - 로그인 한 유저와 펫 소유자가 일치하지 않으면 403 에러를 반환한다.")
    void updateRepresentPetReturn403IsNotMe() throws Exception {
        // given
        Member tester1 = createMember("tester1");
        Pet pet = Pet.builder()
                .petType(PetType.getRandomPetType())
                .rarity(Rarity.COMMON)
                .build();
        tester1.addPet(pet);
        petRepository.save(pet);

        // when / then
        mockMvc.perform(
                        patch(MY_PET_BASE_URL + "/represent-pet")
                                .queryParam("petId", String.valueOf(pet.getId()))
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value(ErrorCode.FORBIDDEN.name()))
                .andExpect(jsonPath("message").value(ErrorCode.FORBIDDEN.getMessage()));
    }

    @Test
    @DisplayName("PATCH /api/pets/my/represent-pet?petId=#petId - 해당 펫이 없으면 404 에러를 반환한다.")
    void updateRepresentPetReturn404ByNotFoundPet() throws Exception {
        // when / then
        mockMvc.perform(
                        patch(MY_PET_BASE_URL + "/represent-pet", member.getId())
                                .queryParam("petId", String.valueOf(1L))
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("code").value(ErrorCode.NOT_FOUND_PET.name()))
                .andExpect(jsonPath("message").value(ErrorCode.NOT_FOUND_PET.getMessage()));
    }

    @Test
    @DisplayName("PATCH /api/pets/my/{petId}/feed - 펫에게 먹이를 준다.")
    void feedToPet() throws Exception {
        // given
        Pet pet = Pet.builder()
                .petType(PetType.getRandomPetType())
                .rarity(Rarity.COMMON)
                .build();
        member.addPet(pet);
        member.addFood(10);
        petRepository.save(pet);

        // when / then
        mockMvc.perform(
                        patch(MY_PET_BASE_URL + "/{petId}/feed", pet.getId())
                                .queryParam("foodCnt", String.valueOf(1))
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PATCH /api/pets/my/{petId}/feed - 펫 정보가 없으면 404 에러를 반환한다.")
    void feedToPetReturn404WhenIsNoPet() throws Exception {
        // given

        // when / then
        mockMvc.perform(
                        patch( MY_PET_BASE_URL + "/{petId}/feed", 1L)
                                .queryParam("foodCnt", String.valueOf(1))
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("code").value(ErrorCode.NOT_FOUND_PET.name()))
                .andExpect(jsonPath("message").value(ErrorCode.NOT_FOUND_PET.getMessage()));
    }

    @Test
    @DisplayName("PATCH /api/pets/my/{petId}/feed 요청 시 유저가 가진 음식 수보다 많은 양을 요청하면 400 에러를 반환한다.")
    void feedToPetReturn400ByOverFood() throws Exception {
        // given
        Pet pet = Pet.builder()
                .petType(PetType.getRandomPetType())
                .rarity(Rarity.COMMON)
                .build();
        member.addPet(pet);
        member.addFood(10);
        petRepository.save(pet);

        // when / then
        mockMvc.perform(
                        patch(MY_PET_BASE_URL + "/{petId}/feed", pet.getId())
                                .queryParam("foodCnt", String.valueOf(11))
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("code").value(ErrorCode.OVER_FOOD_CNT.name()))
                .andExpect(jsonPath("message").value(ErrorCode.OVER_FOOD_CNT.getMessage()));
    }

    @Test
    @DisplayName("PATCH /api/pets/{petId}/feed 요청 시 펫의 소유자가 아니면 403 에러를 반환한다.")
    void feedToPetReturn403() throws Exception {
        // given
        Member tester1 = createMember("tester1");
        Pet pet = Pet.builder()
                .petType(PetType.getRandomPetType())
                .rarity(Rarity.COMMON)
                .build();
        tester1.addPet(pet);
        tester1.addFood(10);
        petRepository.save(pet);

        // when / then
        mockMvc.perform(
                        patch(MY_PET_BASE_URL + "/{petId}/feed", pet.getId())
                                .queryParam("foodCnt", String.valueOf(1))
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value(ErrorCode.FORBIDDEN.name()))
                .andExpect(jsonPath("message").value(ErrorCode.FORBIDDEN.getMessage()));
    }

    @Test
    @DisplayName("DELETE /api/pets/my/{petId} 요청 시 펫을 삭제한다.")
    void deletePet() throws Exception {
        // given
        Pet pet = Pet.builder()
                .petType(PetType.getRandomPetType())
                .rarity(Rarity.COMMON)
                .build();
        member.addPet(pet);
        petRepository.save(pet);

        // when / then
        mockMvc.perform(
                        delete(MY_PET_BASE_URL + "/{petId}", pet.getId())
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/pets/my/{petId} 요청 시 펫의 소유자가 아니면 403 에러를 반환한다.")
    void deletePet403() throws Exception {
        // given
        Member tester1 = createMember("tester1");
        Pet pet = Pet.builder()
                .petType(PetType.getRandomPetType())
                .rarity(Rarity.COMMON)
                .build();
        tester1.addPet(pet);
        petRepository.save(pet);

        // when / then
        mockMvc.perform(
                        delete(MY_PET_BASE_URL + "/{petId}", pet.getId())
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value(ErrorCode.FORBIDDEN.name()))
                .andExpect(jsonPath("message").value(ErrorCode.FORBIDDEN.getMessage()));
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