package com.maruhxn.todomon.global.schedule;

import com.maruhxn.todomon.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.domain.member.domain.Member;
import com.maruhxn.todomon.domain.todo.dao.TodoAchievementHistoryRepository;
import com.maruhxn.todomon.domain.todo.domain.TodoAchievementHistory;
import com.maruhxn.todomon.global.auth.model.Role;
import com.maruhxn.todomon.global.auth.model.provider.OAuth2Provider;
import com.maruhxn.todomon.util.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("[Service] - ScheduleService")
class ScheduleServiceTest extends IntegrationTestSupport {

    @Autowired
    ScheduleService scheduleService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TodoAchievementHistoryRepository todoAchievementHistoryRepository;

    @Test
    @DisplayName("매일 오전 00시에 모든 유저들의 전날 투두 수행 수를 저장한다.")
    void saveDailyAchievementAndReset() {
        // given
        Member member1 = createMember(1);
        Member member2 = createMember(2);
        Member member3 = createMember(3);

        member1.addDailyAchievementCnt(1);
        member2.addDailyAchievementCnt(2);
        member3.addDailyAchievementCnt(3);

        memberRepository.saveAll(List.of(member1, member2, member3));

        // when
        scheduleService.saveDailyAchievementAndReset();

        // then
        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<TodoAchievementHistory> histories = todoAchievementHistoryRepository.findAll();

        assertThat(histories)
                .hasSize(3)
                .extracting("member", "cnt", "date")
                .containsExactlyInAnyOrder(
                        tuple(member1, 1L, yesterday),
                        tuple(member2, 2L, yesterday),
                        tuple(member3, 3L, yesterday)
                );
    }

    private Member createMember(int index) {
        String username = "tester" + index;
        Member member = Member.builder()
                .username(username)
                .email(username + "@test.com")
                .provider(OAuth2Provider.GOOGLE)
                .providerId("google_" + username)
                .role(Role.ROLE_USER)
                .profileImageUrl("profileImageUrl")
                .build();
        member.initDiligence();
        return member;
    }
}