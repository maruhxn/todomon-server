package com.maruhxn.todomon.domain.member.api;

import com.maruhxn.todomon.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.domain.member.domain.Member;
import com.maruhxn.todomon.domain.pet.dao.PetRepository;
import com.maruhxn.todomon.domain.pet.domain.Pet;
import com.maruhxn.todomon.domain.pet.domain.PetType;
import com.maruhxn.todomon.domain.pet.domain.Rarity;
import com.maruhxn.todomon.global.auth.model.Role;
import com.maruhxn.todomon.global.auth.model.provider.OAuth2Provider;
import com.maruhxn.todomon.global.error.ErrorCode;
import com.maruhxn.todomon.util.ControllerIntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.maruhxn.todomon.global.auth.application.JwtProvider.BEARER_PREFIX;
import static com.maruhxn.todomon.global.common.Constants.ACCESS_TOKEN_HEADER;
import static com.maruhxn.todomon.global.common.Constants.REFRESH_TOKEN_HEADER;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@DisplayName("[Integration] - RepresentPet")
class RepresentPetIntegrationTest extends ControllerIntegrationTestSupport {

    static final String REPRESENT_PET_BASE_URL = "/api/members/{memberId}/represent-pet";

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PetRepository petRepository;

    @Test
    @DisplayName("PATCH /api/members/{memberId}/represent-pet?petId=#petId 요청 시 해당 펫을 대표 펫으로 설정한다.")
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
                        patch(REPRESENT_PET_BASE_URL, member.getId())
                                .queryParam("petId", String.valueOf(pet.getId()))
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PATCH /api/members/{memberId}/represent-pet?petId=#petId 요청 시 로그인 한 유저의 아이디와 memberId가 일치하지 않으면 403 에러를 반환한다.")
    void updateRepresentPetReturn403IsNotMe() throws Exception {
        // given
        Member tester1 = createMember("tester1");
        Pet pet = Pet.builder()
                .petType(PetType.getRandomPetType())
                .rarity(Rarity.COMMON)
                .build();
        member.addPet(pet);
        petRepository.save(pet);

        // when / then
        mockMvc.perform(
                        patch(REPRESENT_PET_BASE_URL, tester1.getId())
                                .queryParam("petId", String.valueOf(pet.getId()))
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value(ErrorCode.FORBIDDEN.name()))
                .andExpect(jsonPath("message").value(ErrorCode.FORBIDDEN.getMessage()));
    }

    @Test
    @DisplayName("PATCH /api/members/{memberId}/represent-pet?petId=#petId 요청 시 해당 펫이 없으면 404 에러를 반환한다.")
    void updateRepresentPetReturn404ByNotFoundPet() throws Exception {
        // when / then
        mockMvc.perform(
                        patch(REPRESENT_PET_BASE_URL, member.getId())
                                .queryParam("petId", String.valueOf(1L))
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("code").value(ErrorCode.NOT_FOUND_PET.name()))
                .andExpect(jsonPath("message").value(ErrorCode.NOT_FOUND_PET.getMessage()));
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