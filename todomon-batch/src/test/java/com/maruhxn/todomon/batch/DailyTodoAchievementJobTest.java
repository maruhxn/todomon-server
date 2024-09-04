package com.maruhxn.todomon.batch;

import com.maruhxn.todomon.batch.vo.MemberAchievementDTO;
import com.maruhxn.todomon.core.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.todo.dao.TodoAchievementHistoryRepository;
import com.maruhxn.todomon.core.global.auth.model.Role;
import com.maruhxn.todomon.core.global.auth.model.provider.OAuth2Provider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBatchTest
@SpringBootTest
@Import(TestBatchConfig.class)
public class DailyTodoAchievementJobTest {

    @Autowired
    JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TodoAchievementHistoryRepository todoAchievementHistoryRepository;

    @AfterEach
    void tearDown() {
        memberRepository.deleteAllInBatch();
        todoAchievementHistoryRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("DailyTodoAchievementJob 테스트")
    void dailyTodoAchievementJobTest() throws Exception {
        // given
        List<Member> members = new ArrayList<>();
        IntStream.rangeClosed(1, 500)
                .forEach(i -> {
                    Member member = Member.builder()
                            .username("tester" + i)
                            .email("test" + i + "@test.com")
                            .provider(OAuth2Provider.GOOGLE)
                            .providerId("google_foobarfoobar" + i)
                            .role(Role.ROLE_USER)
                            .profileImageUrl("profileImageUrl")
                            .build();
                    member.addDailyAchievementCnt(10);
                    member.addScheduledReward(1000);

                    members.add(member);
                });

        memberRepository.saveAll(members);


        JobParameters jobParameters = new JobParametersBuilder()
                .addString("date", "2024-01-01")
                .toJobParameters();

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // then
        List<MemberAchievementDTO> processedMembers = (List<MemberAchievementDTO>) jobExecution.getExecutionContext().get("processedMembers");
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
        assertThat(processedMembers.size()).isEqualTo(500);
        assertThat(todoAchievementHistoryRepository.findAll().size()).isEqualTo(500);
    }
}
