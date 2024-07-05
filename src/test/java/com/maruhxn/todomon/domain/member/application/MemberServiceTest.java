package com.maruhxn.todomon.domain.member.application;

import com.maruhxn.todomon.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.domain.member.domain.Member;
import com.maruhxn.todomon.global.auth.model.Role;
import com.maruhxn.todomon.global.auth.model.provider.GoogleUser;
import com.maruhxn.todomon.global.auth.model.provider.OAuth2Provider;
import com.maruhxn.todomon.global.error.ErrorCode;
import com.maruhxn.todomon.global.error.exception.NotFoundException;
import com.maruhxn.todomon.util.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("[Service] - MemberService")
class MemberServiceTest extends IntegrationTestSupport {

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    @Test
    @DisplayName("유저 정보를 찾아 반환한다.")
    void findMemberByEmail() {
        // given
        Member member = Member.builder()
                .username("tester")
                .email("test@test.com")
                .role(Role.ROLE_USER)
                .providerId("google_foobarfoobar")
                .provider(OAuth2Provider.GOOGLE)
                .profileImageUrl("google-user-picutre-url")
                .build();
        memberRepository.save(member);

        // when
        Member findMember = memberService.findMemberByEmail(member.getEmail());

        // then
        assertThat(findMember).isEqualTo(member);
    }

    @Test
    @DisplayName("유저 정보가 없으면 NotFoundException을 반환한다.")
    void findMemberByEmailWithNotExistingEmail() {
        // when / then
        assertThatThrownBy(() -> memberService.findMemberByEmail("not-existing@email.com"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(ErrorCode.NOT_FOUND_MEMBER.getMessage());
    }

    @Test
    @DisplayName("OAuth2 유저 정보를 받아 Member Entity를 생성한다.")
    void createOrUpdate() {
        // given
        GoogleUser googleUser = getGoogleUser("test@test.com");

        // when
        Member member = memberService.createOrUpdate(googleUser);

        // then
        Member findMember = memberRepository.findById(member.getId()).get();
        assertThat(member).isEqualTo(findMember);
    }

    @Test
    @DisplayName("OAuth2 유저 정보와 일치하는 Member가 이미 존재하는 경우, 해당 Member의 정보를 수정한다.")
    void createOrUpdate_update() {
        // given
        Member existingMember = Member.builder()
                .username("existing")
                .email("test@test.com")
                .role(Role.ROLE_USER)
                .providerId("google_foobarfoobar")
                .provider(OAuth2Provider.GOOGLE)
                .profileImageUrl("existing-google-user-picutre-url")
                .build();
        memberRepository.save(existingMember);
        GoogleUser googleUser = getGoogleUser("test@test.com");

        // when
        memberService.createOrUpdate(googleUser);

        // then
        Member findMember = memberRepository.findById(existingMember.getId()).get();
        assertThat(findMember.getUsername()).isEqualTo("tester");
        assertThat(findMember.getProfileImageUrl()).isEqualTo("google-user-picutre-url");
    }

    @Test
    @DisplayName("admin 이메일에 해당하는 계정일 경우, 어드민 권한으로 생성된다.")
    void adminCreate() {
        // given
        GoogleUser googleUser = getGoogleUser("maruhan1016@gmail.com");

        // when
        Member member = memberService.createOrUpdate(googleUser);

        // then
        assertThat(member.getRole()).isEqualTo(Role.ROLE_ADMIN);
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
}