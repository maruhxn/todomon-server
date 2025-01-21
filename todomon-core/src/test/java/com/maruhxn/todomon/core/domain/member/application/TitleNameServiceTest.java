package com.maruhxn.todomon.core.domain.member.application;

import com.maruhxn.todomon.core.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.core.domain.member.dao.TitleNameRepository;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.member.domain.TitleName;
import com.maruhxn.todomon.core.domain.member.dto.request.UpsertTitleNameReq;
import com.maruhxn.todomon.core.global.auth.model.Role;
import com.maruhxn.todomon.core.global.auth.model.provider.OAuth2Provider;
import com.maruhxn.todomon.core.util.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("[Service] - TitleNameService")
class TitleNameServiceTest extends IntegrationTestSupport {

    @Autowired
    TitleNameService titleNameService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TitleNameRepository titleNameRepository;

    @Test
    @DisplayName("칭호를 생성한다")
    void createTitleName() {
        // given
        Member member = createMember("tester");
        UpsertTitleNameReq req = UpsertTitleNameReq.builder()
                .name("name")
                .color("#000000")
                .build();

        // when
        titleNameService.upsertTitleName(member, req);

        // then
        assertThat(titleNameRepository.findAll())
                .hasSize(1)
                .first()
                .extracting("name", "color", "member")
                .containsExactly("name", "#000000", member);
    }

    @Test
    @DisplayName("유저의 현재 칭호를 수정한다")
    void updateTitleName() {
        // given
        Member member = createMember("tester");
        TitleName titleName = TitleName.builder()
                .name("name")
                .color("#000000")
                .member(member)
                .build();
        member.setTitleName(titleName);
        titleNameRepository.save(titleName);

        UpsertTitleNameReq req = UpsertTitleNameReq.builder()
                .name("name!")
                .color("#FFFFFF")
                .build();
        // when
        titleNameService.upsertTitleName(member, req);

        // then
        assertThat(titleNameRepository.findAll())
                .hasSize(1)
                .first()
                .extracting("name", "color", "member")
                .containsExactly("name!", "#FFFFFF", member);
    }

    @Test
    @DisplayName("유저의 현재 칭호를 삭제한다")
    void deleteTitleName() {
        // given
        Member member = createMember("tester");
        TitleName titleName = TitleName.builder()
                .name("name")
                .color("#000000")
                .member(member)
                .build();
        member.setTitleName(titleName);
        titleNameRepository.save(titleName);
        // when
        titleNameService.deleteTitleName(member.getId());

        // then
        assertThat(titleNameRepository.findAll()).hasSize(0);
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