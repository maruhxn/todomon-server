package com.maruhxn.todomon.batch;

import com.maruhxn.todomon.core.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.core.domain.todo.dao.TodoAchievementHistoryRepository;
import com.maruhxn.todomon.core.global.auth.model.Role;
import com.maruhxn.todomon.core.global.auth.model.provider.OAuth2Provider;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
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

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
    private int batchSize;

    @AfterEach
    void tearDown() {
        memberRepository.deleteAllInBatch();
        todoAchievementHistoryRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("DailyTodoAchievementJob 테스트")
    void dailyTodoAchievementJobTest() throws Exception {
        // given
        int totalSize = 100000;
        log.info("batch size : {}", batchSize);
        log.info("members size : {}", totalSize);

        for (int batchCount = 0; batchCount < totalSize / batchSize; batchCount++) {
            batchInsert(batchCount);
            log.info("batchCount : {}", batchCount);
        }

        log.info("배치 INSERT 작업 완료");

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("date", "2024-01-16")
                .toJobParameters();

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // then
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
        assertThat(todoAchievementHistoryRepository.findAll().size()).isEqualTo(totalSize);
    }

    private void batchInsert(int batchCount) {
        String sql = "INSERT INTO member" +
                " (username, email, provider, provider_id, role, profile_image_url, created_at, updated_at, daily_achievement_cnt, scheduled_reward)" +
                " VALUES (?, ?, ?, ?, ?, ?, now(), now(), ?, ?)";
        int finalBatchCount = batchCount;
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                int index = 1000 * finalBatchCount + i;
                ps.setString(1, "tester" + index);
                ps.setString(2, "test" + index + "@test.com");
                ps.setString(3, OAuth2Provider.GOOGLE.name());
                ps.setString(4, "google_foobarfoobar" + index);
                ps.setString(5, Role.ROLE_USER.name());
                ps.setString(6, "img");
                ps.setLong(7, 10);
                ps.setLong(8, 1000);
            }

            @Override
            public int getBatchSize() {
                return batchSize;
            }
        });
    }
}
