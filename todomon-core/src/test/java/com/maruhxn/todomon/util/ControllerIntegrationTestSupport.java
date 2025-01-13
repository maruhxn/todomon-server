package com.maruhxn.todomon.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maruhxn.todomon.config.MockS3Config;
import com.maruhxn.todomon.config.TestConfig;
import com.maruhxn.todomon.core.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.core.domain.member.dao.TitleNameRepository;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.member.domain.TitleName;
import com.maruhxn.todomon.core.global.auth.implement.JwtProvider;
import com.maruhxn.todomon.core.global.auth.dto.MemberDTO;
import com.maruhxn.todomon.core.global.auth.dto.TokenDto;
import com.maruhxn.todomon.core.global.auth.model.Role;
import com.maruhxn.todomon.core.global.auth.model.TodomonOAuth2User;
import com.maruhxn.todomon.core.global.auth.model.provider.OAuth2Provider;
import com.maruhxn.todomon.core.infra.file.FileService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({MockS3Config.class, TestConfig.class})
public abstract class ControllerIntegrationTestSupport extends RedisTestContainersConfig {
    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    protected FileService fileService;

    @Autowired
    protected JwtProvider jwtProvider;

    @Autowired
    protected MemberRepository memberRepository;

    @Autowired
    protected TitleNameRepository titleNameRepository;

    @Autowired
    private RedisTemplate redisTemplate;

    protected Member member;
    protected Member admin;
    protected TokenDto adminTokenDto;
    protected TokenDto memberTokenDto;

    @BeforeEach
    void setUp(
            final WebApplicationContext context
    ) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .alwaysDo(print())
                .build();

        member = Member.builder()
                .username("tester")
                .email("test@test.com")
                .provider(OAuth2Provider.GOOGLE)
                .providerId("google_foobarfoobar")
                .role(Role.ROLE_USER)
                .profileImageUrl("profileImageUrl")
                .build();
        member.initDiligence();

        admin = Member.builder()
                .username("admin")
                .email("admin@admin.com")
                .provider(OAuth2Provider.GOOGLE)
                .providerId("google_admin")
                .role(Role.ROLE_ADMIN)
                .profileImageUrl("profileImageUrl")
                .build();
        admin.initDiligence();

        memberRepository.saveAll(List.of(member, admin));

        memberTokenDto = getTokenDto(member);
        adminTokenDto = getTokenDto(admin);
    }

    @AfterEach
    void cleanUp() {
        redisTemplate.delete(redisTemplate.keys("*"));
    }

    private TokenDto getTokenDto(Member member) {
        MemberDTO dto = MemberDTO.from(member);
        TodomonOAuth2User todomonOAuth2User = TodomonOAuth2User.from(dto);
        return jwtProvider.createJwt(todomonOAuth2User);
    }

    protected TitleName createTitleName(Member member) {
        TitleName titleName = TitleName.builder()
                .name("TEST")
                .color("#000000")
                .member(member)
                .build();
        member.setTitleName(titleName);
        return titleNameRepository.save(titleName);
    }
}
