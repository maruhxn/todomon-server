package com.maruhxn.todomon.batch;

import com.maruhxn.todomon.core.global.auth.model.Role;
import com.maruhxn.todomon.core.global.auth.model.provider.OAuth2Provider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("dev")
public class MockDataLoader {

    private final JdbcTemplate jdbcTemplate;

    @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
    private int batchSize;

    private static final int TOTAL_SIZE = 100000;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void init() {
        long beforeTime = System.currentTimeMillis();
        log.info("batch size : {}", batchSize);
        log.info("members size : {}", TOTAL_SIZE);

        for (int batchCount = 0; batchCount < TOTAL_SIZE / batchSize; batchCount++) {
            batchInsertMember(batchCount);
            log.info("batchCount : {}", batchCount);
        }

        long afterTime = System.currentTimeMillis(); // 코드 실행 후에 시간 받아오기
        long diffTime = afterTime - beforeTime; // 두 개의 실행 시간
        log.info("실행 시간(ms) = {}", diffTime);
    }

    private void batchInsertMember(int batchCount) {
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
