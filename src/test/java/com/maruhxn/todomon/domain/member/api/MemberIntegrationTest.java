package com.maruhxn.todomon.domain.member.api;

import com.maruhxn.todomon.domain.member.dao.TitleNameRepository;
import com.maruhxn.todomon.domain.member.domain.Member;
import com.maruhxn.todomon.domain.member.domain.TitleName;
import com.maruhxn.todomon.domain.pet.dao.PetRepository;
import com.maruhxn.todomon.domain.pet.domain.Pet;
import com.maruhxn.todomon.domain.pet.domain.PetType;
import com.maruhxn.todomon.domain.pet.domain.Rarity;
import com.maruhxn.todomon.domain.social.dao.FollowRepository;
import com.maruhxn.todomon.domain.social.domain.Follow;
import com.maruhxn.todomon.domain.social.domain.FollowRequestStatus;
import com.maruhxn.todomon.global.auth.model.Role;
import com.maruhxn.todomon.global.auth.model.provider.OAuth2Provider;
import com.maruhxn.todomon.global.error.ErrorCode;
import com.maruhxn.todomon.util.ControllerIntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import static com.maruhxn.todomon.global.auth.application.JwtProvider.BEARER_PREFIX;
import static com.maruhxn.todomon.global.common.Constants.ACCESS_TOKEN_HEADER;
import static com.maruhxn.todomon.global.common.Constants.REFRESH_TOKEN_HEADER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("[Integration] - Member")
class MemberIntegrationTest extends ControllerIntegrationTestSupport {

    static final String MEMBER_BASE_URL = "/api/members";

    @Autowired
    PetRepository petRepository;

    @Autowired
    TitleNameRepository titleNameRepository;

    @Autowired
    FollowRepository followRepository;

    @Test
    @DisplayName("GET /api/members/{memberId} 요청 시 해당 유저의 프로필 정보를 조회한다.")
    void getProfile() throws Exception {
        // given
        Member tester1 = createMember("tester1");
        Member tester2 = createMember("tester2");
        Member tester3 = createMember("tester3");
        Member tester4 = createMember("tester4");

        Pet pet = Pet.builder()
                .petType(PetType.getRandomPetType())
                .rarity(Rarity.COMMON)
                .build();
        tester1.addPet(pet);
        tester1.setRepresentPet(pet);
        petRepository.save(pet);

        TitleName titleName = TitleName.builder()
                .member(tester1)
                .name("title")
                .color("#000000")
                .build();
        titleName.setMember(tester1);
        titleNameRepository.save(titleName);

        Follow following1 = Follow.builder()
                .follower(tester1)
                .followee(tester2)
                .build();
        Follow following2 = Follow.builder()
                .follower(tester1)
                .followee(tester3)
                .build();
        Follow followed = Follow.builder()
                .follower(tester4)
                .followee(tester1)
                .build();

        following1.updateStatus(FollowRequestStatus.ACCEPTED);
        following2.updateStatus(FollowRequestStatus.ACCEPTED);
        followed.updateStatus(FollowRequestStatus.ACCEPTED);

        followRepository.saveAll(List.of(followed, following1, following2));

        // when / then
        mockMvc.perform(
                        get(MEMBER_BASE_URL + "/{memberId}", tester1.getId())
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("OK"))
                .andExpect(jsonPath("message").value("프로필 조회 성공"))
                .andExpect(jsonPath("data.id").value(tester1.getId()))
                .andExpect(jsonPath("data.username").value("tester1"))
                .andExpect(jsonPath("data.email").value("tester1@test.com"))
                .andExpect(jsonPath("data.profileImageUrl").value("profileImageUrl"))
                .andExpect(jsonPath("data.level").value(1))
                .andExpect(jsonPath("data.gauge").value(0.0))
                .andExpect(jsonPath("data.title.name").value("title"))
                .andExpect(jsonPath("data.title.color").value("#000000"))
                .andExpect(jsonPath("data.followInfo.followerCnt").value(1L))
                .andExpect(jsonPath("data.followInfo.followingCnt").value(2L))
                .andExpect(jsonPath("data.followInfo.isFollowing").value(false))
                .andExpect(jsonPath("data.followInfo.receivedRequestId").isEmpty())
                .andExpect(jsonPath("data.followInfo.receivedFollowStatus").isEmpty())
                .andExpect(jsonPath("data.followInfo.sentFollowStatus").isEmpty())
                .andExpect(jsonPath("data.representPetItem.id").value(pet.getId()))
                .andExpect(jsonPath("data.representPetItem.name").value(pet.getName()))
                .andExpect(jsonPath("data.representPetItem.rarity").value(pet.getRarity().name()))
                .andExpect(jsonPath("data.representPetItem.appearance").value(pet.getAppearance()))
                .andExpect(jsonPath("data.representPetItem.color").value(pet.getColor()))
                .andExpect(jsonPath("data.representPetItem.level").value(pet.getLevel()));

    }

    @Test
    @DisplayName("GET /api/members/{memberId} 요청 시 존재하지 않는 유저의 경우 404 에러를 반환한다.")
    void getProfileReturn404() throws Exception {
        // when / then
        mockMvc.perform(
                        get(MEMBER_BASE_URL + "/{memberId}", 0)
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("code").value(ErrorCode.NOT_FOUND_MEMBER.name()))
                .andExpect(jsonPath("message").value(ErrorCode.NOT_FOUND_MEMBER.getMessage()));
    }

    @Test
    @DisplayName("PATCH /api/members/{memberId} 요청 시 해당 유저의 프로필을 변경한다.")
    void updateProfile() throws Exception {
        // given
        MockMultipartFile newProfileImage = getMockMultipartFile();
        given(fileService.storeOneFile(any(MultipartFile.class)))
                .willReturn("newProfileImageUrl");

        // when / then
        mockMvc.perform(
                        multipart(HttpMethod.PATCH, MEMBER_BASE_URL + "/{memberId}", member.getId())
                                .file(newProfileImage)
                                .part(new MockPart("username", "update!".getBytes()))
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PATCH /api/members/{memberId} 요청 시 어드민은 해당 유저의 프로필을 변경할 수 있다.")
    void updateProfileByAdmin() throws Exception {
        // given
        MockMultipartFile newProfileImage = getMockMultipartFile();
        given(fileService.storeOneFile(any(MultipartFile.class)))
                .willReturn("newProfileImageUrl");

        // when / then
        mockMvc.perform(
                        multipart(HttpMethod.PATCH, MEMBER_BASE_URL + "/{memberId}", member.getId())
                                .file(newProfileImage)
                                .part(new MockPart("username", "update!".getBytes()))
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + adminTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + adminTokenDto.getRefreshToken())
                )
                .andExpect(status().isNoContent());
    }


    @Test
    @DisplayName("PATCH /api/members/{memberId} 요청 시 수정할 데이터를 전달하지 않으면 400 에러를 반환한다.")
    void updateProfileReturn400WithoutData() throws Exception {
        // given

        // when / then
        mockMvc.perform(
                        multipart(HttpMethod.PATCH, MEMBER_BASE_URL + "/{memberId}", member.getId())
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("code").value(ErrorCode.VALIDATION_ERROR.name()))
                .andExpect(jsonPath("message").value("수정할 데이터를 전달해야 합니다."));
    }

    @Test
    @DisplayName("PATCH /api/members/{memberId} 요청 시 로그인한 유저와 요청받은 유저가 일치하지 않으면 403 에러를 반환한다.")
    void updateProfileReturn403WhenIsNotMe() throws Exception {
        // given
        Member tester1 = createMember("tester1");

        // when / then
        mockMvc.perform(
                        multipart(HttpMethod.PATCH, MEMBER_BASE_URL + "/{memberId}", tester1.getId())
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value(ErrorCode.FORBIDDEN.name()))
                .andExpect(jsonPath("message").value(ErrorCode.FORBIDDEN.getMessage()));
    }


    @Test
    @DisplayName("DELETE /api/members/{memberId} 요청 시 회원탈퇴를 진행한다.")
    void withdraw() throws Exception {
        mockMvc.perform(
                        delete(MEMBER_BASE_URL + "/{memberId}", member.getId())
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/members/{memberId} 요청 시 어드민은 회원을 탈퇴시킬 수 있다.")
    void withdrawByAdmin() throws Exception {
        mockMvc.perform(
                        delete(MEMBER_BASE_URL + "/{memberId}", member.getId())
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + adminTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + adminTokenDto.getRefreshToken())
                )
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/members/{memberId} 요청 시 로그인한 유저와 요청받은 유저가 일치하지 않으면 403 에러를 반환한다.")
    void withdrawReturn403WhenIsNotMe() throws Exception {
        // given
        Member tester1 = createMember("tester1");

        // when / then
        mockMvc.perform(
                        delete(MEMBER_BASE_URL + "/{memberId}", tester1.getId())
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value(ErrorCode.FORBIDDEN.name()))
                .andExpect(jsonPath("message").value(ErrorCode.FORBIDDEN.getMessage()));
    }

    private MockMultipartFile getMockMultipartFile() throws IOException {
        final String originalFileName = "profile.png";
        final String filePath = "src/test/resources/static/img/" + originalFileName;

        return new MockMultipartFile(
                "profileImage", //name
                originalFileName,
                "image/jpeg",
                new FileInputStream(filePath)
        );
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