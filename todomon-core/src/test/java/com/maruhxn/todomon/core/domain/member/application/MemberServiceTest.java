package com.maruhxn.todomon.core.domain.member.application;

import com.maruhxn.todomon.core.domain.auth.dao.RefreshTokenRepository;
import com.maruhxn.todomon.core.domain.auth.domain.RefreshToken;
import com.maruhxn.todomon.core.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.core.domain.member.dao.TitleNameRepository;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.member.domain.TitleName;
import com.maruhxn.todomon.core.domain.member.dto.request.UpdateMemberProfileReq;
import com.maruhxn.todomon.core.domain.member.dto.response.ProfileRes;
import com.maruhxn.todomon.core.domain.pet.dao.PetRepository;
import com.maruhxn.todomon.core.domain.pet.domain.Pet;
import com.maruhxn.todomon.core.domain.pet.domain.PetType;
import com.maruhxn.todomon.core.domain.pet.domain.Rarity;
import com.maruhxn.todomon.core.domain.social.dao.FollowRepository;
import com.maruhxn.todomon.core.domain.social.domain.Follow;
import com.maruhxn.todomon.core.domain.social.domain.FollowRequestStatus;
import com.maruhxn.todomon.core.global.auth.implement.JwtProvider;
import com.maruhxn.todomon.core.global.auth.model.Role;
import com.maruhxn.todomon.core.global.auth.model.provider.GoogleUser;
import com.maruhxn.todomon.core.global.auth.model.provider.OAuth2Provider;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.ForbiddenException;
import com.maruhxn.todomon.util.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@DisplayName("[Service] - MemberService")
class MemberServiceTest extends IntegrationTestSupport {

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @Autowired
    TitleNameRepository titleNameRepository;

    @Autowired
    FollowRepository followRepository;

    @Autowired
    PetRepository petRepository;

    @Autowired
    JwtProvider jwtProvider;

    @Test
    @DisplayName("OAuth2 유저 정보를 받아 Member Entity를 생성한다.")
    void createOrUpdate() {
        // given
        GoogleUser googleUser = getGoogleUser("test@test.com");

        // when
        Member member = memberService.getOrCreate(googleUser);

        // then
        Member findMember = memberRepository.findById(member.getId()).get();
        assertThat(member).isEqualTo(findMember);
    }

    @Test
    @DisplayName("이미 존재하는 이름의 유저 가입 시, providerId를 이름으로 설정하여 엔터티를 생성한다")
    void updateUsernameToProviderId() {
        // given
        memberRepository.save(Member.builder()
                .username("tester")
                .provider(OAuth2Provider.KAKAO)
                .providerId("foo")
                .email("existing-tester@test.com")
                .profileImageUrl("profileImage")
                .build());
        GoogleUser googleUser = getGoogleUser("test@test.com");

        // when
        Member member = memberService.getOrCreate(googleUser);

        // then
        assertThat(member.getUsername()).isEqualTo(googleUser.getProviderId());
    }

    @Test
    @DisplayName("admin 이메일에 해당하는 계정일 경우, 어드민 권한으로 생성된다.")
    void adminCreate() {
        // given
        GoogleUser googleUser = getGoogleUser("maruhan1016@gmail.com");

        // when
        Member member = memberService.getOrCreate(googleUser);

        // then
        assertThat(member.getRole()).isEqualTo(Role.ROLE_ADMIN);
    }

    @Test
    @DisplayName("유저 프로필을 조회한다.")
    void getProfile() {
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
        tester1.setTitleName(titleName);
        titleNameRepository.save(titleName);

        Follow following1 = Follow.builder()
                .follower(tester1)
                .followee(tester2)
                .build();
        Follow following2 = Follow.builder()
                .follower(tester1)
                .followee(tester3)
                .build();
        Follow followed1 = Follow.builder()
                .follower(tester4)
                .followee(tester1)
                .build();
        Follow followed2 = Follow.builder()
                .follower(tester2)
                .followee(tester1)
                .build();

        following1.updateStatus(FollowRequestStatus.ACCEPTED);
        following2.updateStatus(FollowRequestStatus.ACCEPTED);
        followed1.updateStatus(FollowRequestStatus.ACCEPTED);

        followRepository.saveAll(List.of(followed1, followed2, following1, following2));

        // when
        ProfileRes profile = memberService.getProfile(tester2.getId(), tester1.getId());

        // then
        assertThat(profile)
                .satisfies(dto -> {
                    assertThat(dto.getId()).isEqualTo(tester1.getId());
                    assertThat(dto.getUsername()).isEqualTo("tester1");
                    assertThat(dto.getProfileImageUrl()).isEqualTo("profileImageUrl");
                    assertThat(dto.isSubscribed()).isFalse();
                    assertThat(dto.getLevel()).isEqualTo(1);
                    assertThat(dto.getGauge()).isEqualTo(0.0);
                    assertThat(dto.getRepresentPetItem().getId()).isEqualTo(pet.getId());
                    assertThat(dto.getRepresentPetItem().getName()).isEqualTo(pet.getName());
                    assertThat(dto.getRepresentPetItem().getRarity()).isEqualTo(pet.getRarity());
                    assertThat(dto.getRepresentPetItem().getAppearance()).isEqualTo(pet.getAppearance());
                    assertThat(dto.getRepresentPetItem().getColor()).isEqualTo(pet.getColor());
                    assertThat(dto.getRepresentPetItem().getLevel()).isEqualTo(pet.getLevel());
                    assertThat(dto.getTitle().getId()).isEqualTo(titleName.getId());
                    assertThat(dto.getTitle().getName()).isEqualTo(titleName.getName());
                    assertThat(dto.getTitle().getColor()).isEqualTo(titleName.getColor());
                    assertThat(dto.getFollowInfo().getFollowerCnt()).isEqualTo(1L);
                    assertThat(dto.getFollowInfo().getFollowingCnt()).isEqualTo(2L);
                    assertThat(dto.getFollowInfo().getIsFollowing()).isTrue();
                    assertThat(dto.getFollowInfo().getReceivedRequestId()).isEqualTo(following1.getId());
                    assertThat(dto.getFollowInfo().getReceivedFollowStatus()).isEqualTo(FollowRequestStatus.ACCEPTED);
                    assertThat(dto.getFollowInfo().getSentFollowStatus()).isEqualTo(FollowRequestStatus.PENDING);
                });
    }

    @Test
    @DisplayName("유저명을 업데이트한다.")
    void updateUsername() throws IOException {
        // given
        Member member = Member.builder()
                .username("existing")
                .email("test@test.com")
                .role(Role.ROLE_USER)
                .providerId("google_foobarfoobar")
                .provider(OAuth2Provider.GOOGLE)
                .profileImageUrl("existing-google-user-picture-url")
                .build();
        memberRepository.save(member);

        saveMemberToContext(member);

        UpdateMemberProfileReq req = UpdateMemberProfileReq.builder()
                .username("update")
                .build();

        // when
        memberService.updateProfile(member.getId(), req);

        // then
        assertThat(member)
                .extracting("username", "profileImageUrl")
                .containsExactly("update", "existing-google-user-picture-url");
    }

    @Test
    @DisplayName("유저명과 프로필 이미지를 업데이트한다.")
    void updateProfile() throws IOException {
        // given
        Member member = Member.builder()
                .username("existing")
                .email("test@test.com")
                .role(Role.ROLE_USER)
                .providerId("google_foobarfoobar")
                .provider(OAuth2Provider.GOOGLE)
                .profileImageUrl("existing-google-user-picture-url")
                .build();
        memberRepository.save(member);
        saveMemberToContext(member);

        MockMultipartFile newProfileImage = getMockMultipartFile();
        UpdateMemberProfileReq req = UpdateMemberProfileReq.builder()
                .username("update")
                .profileImage(newProfileImage)
                .build();
        given(fileService.storeOneFile(any(MultipartFile.class)))
                .willReturn("newProfileImageUrl");

        // when
        memberService.updateProfile(member.getId(), req);

        // then
        assertThat(member)
                .extracting("username", "profileImageUrl")
                .containsExactly("update", "newProfileImageUrl");
    }

    @Test
    @DisplayName("회원 탈퇴가 가능하다.")
    void membershipWithdrawal() {
        // Given
        Member member = Member.builder()
                .username("existing")
                .email("test@test.com")
                .role(Role.ROLE_USER)
                .providerId("google_foobarfoobar")
                .provider(OAuth2Provider.GOOGLE)
                .profileImageUrl("existing-google-user-picture-url")
                .build();
        memberRepository.save(member);
        saveMemberToContext(member);

        String rawRefreshToken = jwtProvider.generateRefreshToken(member.getUsername(), new Date());
        RefreshToken refreshToken = RefreshToken.builder()
                .payload(rawRefreshToken)
                .username(member.getUsername())
                .build();
        refreshTokenRepository.save(refreshToken);

        // When
        memberService.withdraw(member.getId());

        // Then
        Optional<Member> optionalMember = memberRepository.findById(member.getId());
        List<RefreshToken> refreshTokens = refreshTokenRepository.findAll();
        assertThat(optionalMember.isEmpty()).isTrue();
        assertThat(refreshTokens).isEmpty();
    }

    @DisplayName("회원 탈퇴 시 존재하지 않는 회원의 아이디를 전달하면 에러를 발생한다..")
    @Test
    void membershipWithdrawalWithNonExistingMemberId() {
        Member member = createMember("tester");
        saveMemberToContext(member);
        assertThatThrownBy(() -> memberService.withdraw(member.getId() * 100))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage(ErrorCode.FORBIDDEN.getMessage());
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

    private static GoogleUser getGoogleUser(String email) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "foobarfoobar");
        attributes.put("picture", "google-user-picutre-url");
        attributes.put("email", email);
        attributes.put("name", "tester");
        GoogleUser googleUser = new GoogleUser(attributes, "google");
        return googleUser;
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
}