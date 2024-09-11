package com.maruhxn.todomon.batch;

import com.maruhxn.todomon.batch.job.send_notification.SendNotificationJobConfig;
import com.maruhxn.todomon.core.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.todo.dao.TodoRepository;
import com.maruhxn.todomon.core.domain.todo.domain.Todo;
import com.maruhxn.todomon.core.global.auth.model.Role;
import com.maruhxn.todomon.core.global.auth.model.provider.OAuth2Provider;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ActiveProfiles("test")
@SpringBatchTest
@SpringBootTest(classes = {SendNotificationJobConfig.class, TestBatchConfig.class})
@TestPropertySource(properties = {"chunkSize=500", "poolSize=1", "spring.mail.username=test"})
@EnableJpaAuditing
public class SendNotificationJobTest {

    @Autowired
    JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TodoRepository todoRepository;


    @AfterEach
    void tearDown() {
        todoRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("SendNotificationJob 테스트")
    void sendNotificationJobTest() throws Exception {
        // given
        List<Member> members = IntStream.rangeClosed(1, 10)
                .mapToObj(i -> Member.builder()
                        .username("tester" + i)
                        .email("test" + i + "@test.com")
                        .provider(OAuth2Provider.GOOGLE)
                        .providerId("google_foobarfoobar" + i)
                        .role(Role.ROLE_USER)
                        .profileImageUrl("profileImageUrl")
                        .build()
                ).toList();
        memberRepository.saveAll(members);

        for (int i = 0; i < members.size(); i++) {
            // 1 3 5 7 9
            LocalDateTime startAt = i % 2 == 0 ? LocalDateTime.now() : LocalDateTime.now().plusMinutes(30).plusSeconds(20);

            // 1 5 7
            createSingleTodo(
                    startAt,
                    startAt.plusMinutes(30),
                    i % 3 == 0,
                    members.get(i)
            );
        }

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();

        // then
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
        assertThat(jobExecution.getStepExecutions().iterator().next().getReadCount()).isEqualTo(3);
    }

    public Todo createSingleTodo(LocalDateTime startAt, LocalDateTime endAt, boolean isAllDay, Member member) {
        Todo todo = Todo.builder()
                .content("테스트")
                .startAt(startAt)
                .endAt(endAt)
                .isAllDay(isAllDay)
                .writer(member)
                .build();

        return todoRepository.save(todo);
    }

//    private void batchInsert(int batchCount) {
//        String sql = "INSERT INTO todo" +
//                " (content, startAt, endAt, is_all_day, writer_id, created_at, updated_at)" +
//                " VALUES (?, ?, ?, ?, ?, now(), now())";
//        int finalBatchCount = batchCount;
//        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
//
//            @Override
//            public void setValues(PreparedStatement ps, int i) throws SQLException {
//                int index = batchSize * finalBatchCount + i;
//                LocalDateTime startAt = i % 2 == 0 ? LocalDateTime.now() : LocalDateTime.now().plusMinutes(30);
//
//                ps.setString(1, "test" + index);
//                ps.setDate(2, startAt);
//                ps.setString(3, startAt.plusHours(1));
//                ps.setBoolean(4, false);
//                ps.setString(5, index);
//            }
//
//            @Override
//            public int getBatchSize() {
//                return batchSize;
//            }
//        });
//    }
}
