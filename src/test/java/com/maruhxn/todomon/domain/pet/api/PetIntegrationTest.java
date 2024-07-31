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
    @DisplayName("GET /api/pets/my 요청 시 자신이 소유하고 있는 모든 펫을 조회한다.")
    void getMyPets() throws Exception {
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
                        get(PET_BASE_URL + "/my")
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("OK"))
                .andExpect(jsonPath("message").value("나의 펫 조회 성공"))
                .andExpect(jsonPath("data").isArray())
                .andExpect(jsonPath("data.size()").value(2))
                .andExpect(jsonPath("data[0].petType").value(PetType.DOG.name()))
                .andExpect(jsonPath("data[0].rarity").value(Rarity.COMMON.name()))
                .andExpect(jsonPath("data[0].name").value(pet1.getName()))
                .andExpect(jsonPath("data[0].appearance").value(PetType.DOG.getEvolutionStage(0).getForm()))
                .andExpect(jsonPath("data[0].color").value(Rarity.COMMON.getColor()))
                .andExpect(jsonPath("data[0].level").value(1))
                .andExpect(jsonPath("data[0].gauge").value(0.0));

    }

    @Test
    @DisplayName("GET /api/members/collections 요청 시 획득한 펫 목록을 조회한다.")
    void getMyCollections() throws Exception {
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
                        get(PET_BASE_URL + "/collections")
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
    @DisplayName("PATCH /api/pets/{petId}/feed 요청 시 펫에게 먹일르 준다.")
    void feedToPet() throws Exception {
        // given
        Pet pet = Pet.builder()
                .petType(PetType.getRandomPetType())
                .rarity(Rarity.COMMON)
                .build();
        member.addPet(pet);
        member.addFood(10);
        petRepository.save(pet);

        FeedReq req = new FeedReq(1);

        // when / then
        mockMvc.perform(
                        patch(PET_BASE_URL + "/{petId}/feed", pet.getId())
                                .content(objectMapper.writeValueAsString(req))
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PATCH /api/pets/{petId}/feed 요청 시 펫 정보가 없으면 404 에러를 반환한다.")
    void feedToPetReturn404WhenIsNoPet() throws Exception {
        // given
        FeedReq req = new FeedReq(1);

        // when / then
        mockMvc.perform(
                        patch(PET_BASE_URL + "/{petId}/feed", 1L)
                                .content(objectMapper.writeValueAsString(req))
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("code").value(ErrorCode.NOT_FOUND_PET.name()))
                .andExpect(jsonPath("message").value(ErrorCode.NOT_FOUND_PET.getMessage()));
    }

    @Test
    @DisplayName("PATCH /api/pets/{petId}/feed 요청 시 유저가 가진 음식 수보다 많은 양을 요청하면 400 에러를 반환한다.")
    void feedToPetReturn400ByOverFood() throws Exception {
        // given
        Pet pet = Pet.builder()
                .petType(PetType.getRandomPetType())
                .rarity(Rarity.COMMON)
                .build();
        member.addPet(pet);
        member.addFood(10);
        petRepository.save(pet);
        FeedReq req = new FeedReq(11);

        // when / then
        mockMvc.perform(
                        patch(PET_BASE_URL + "/{petId}/feed", pet.getId())
                                .content(objectMapper.writeValueAsString(req))
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
        FeedReq req = new FeedReq(11);

        // when / then
        mockMvc.perform(
                        patch(PET_BASE_URL + "/{petId}/feed", pet.getId())
                                .content(objectMapper.writeValueAsString(req))
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value(ErrorCode.FORBIDDEN.name()))
                .andExpect(jsonPath("message").value(ErrorCode.FORBIDDEN.getMessage()));
    }

    @Test
    @DisplayName("DELETE /api/members/{petId} 요청 시 펫을 삭제한다.")
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
                        delete(PET_BASE_URL + "/{petId}", pet.getId())
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/members/{petId} 요청 시 펫의 소유자가 아니면 403 에러를 반환한다.")
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
                        delete(PET_BASE_URL + "/{petId}", pet.getId())
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