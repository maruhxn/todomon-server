package com.maruhxn.todomon.util;

import com.maruhxn.todomon.config.TestConfig;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.global.auth.model.TodomonOAuth2User;
import com.maruhxn.todomon.core.infra.file.FileService;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@Import(TestConfig.class)
public abstract class IntegrationTestSupport {

    @MockBean
    protected FileService fileService;


    protected static void saveMemberToContext(Member member) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        TodomonOAuth2User todomonOAuth2User = TodomonOAuth2User.of(member); // 사용자의 커스텀 정보 설정
        OAuth2AuthenticationToken auth = new OAuth2AuthenticationToken(
                todomonOAuth2User,
                todomonOAuth2User.getAuthorities(),
                todomonOAuth2User.getProvider().name()
        );
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

}
