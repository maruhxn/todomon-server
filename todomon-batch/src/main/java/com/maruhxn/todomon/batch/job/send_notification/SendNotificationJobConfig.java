package com.maruhxn.todomon.batch.job.send_notification;

import com.maruhxn.todomon.batch.chunk.writer.SendNotificationWriter;
import com.maruhxn.todomon.batch.listener.StopWatchJobListener;
import com.maruhxn.todomon.infra.mail.MailService;
import com.maruhxn.todomon.infra.mail.dto.SendNotificationBatchDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO: todo + todo_instance까지 모두 조회 필요 -> step 2개? reader 2개?
 * TODO: mail 전송은 외부 서비스 호출 -> Async 도입 필요
 */

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SendNotificationJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager tx;
    private final DataSource dataSource;

    private int chunkSize;
    private int poolSize;

    @Value("${chunkSize:1000}")
    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    @Value("${poolSize:10}")
    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    @Bean
    public Job sendNotificationJob(
            Step sendNotificationStep
    ) throws Exception {
        return new JobBuilder("sendNotificationJob", jobRepository)
                .start(sendNotificationStep)
                .listener(new StopWatchJobListener())
                .build();
    }

    @Bean(name = "sendNotificationStep")
    @JobScope
    public Step sendNotificationStep(
            ItemReader<SendNotificationBatchDTO> sendNotificationReader,
            ItemWriter<SendNotificationBatchDTO> sendNotificationWriter
    ) throws Exception {
        return new StepBuilder("sendNotificationStep", jobRepository)
                .<SendNotificationBatchDTO, SendNotificationBatchDTO>chunk(chunkSize, tx)
                .reader(sendNotificationReader)
                .writer(sendNotificationWriter)
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<SendNotificationBatchDTO> sendNotificationReader(
            PagingQueryProvider sendNotificationQueryProvider
    ) throws Exception {
        return new JdbcPagingItemReaderBuilder<SendNotificationBatchDTO>()
                .name("sendNotificationReader")
                .dataSource(this.dataSource)
                .pageSize(chunkSize)
                .beanRowMapper(SendNotificationBatchDTO.class)
                .queryProvider(sendNotificationQueryProvider)
                .build();
    }

    @Bean
    public PagingQueryProvider sendNotificationQueryProvider() throws Exception {
        SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
        queryProvider.setDataSource(dataSource);
        queryProvider.setSelectClause("t.id, t.start_at, t.content, m.username, m.email");
        queryProvider.setFromClause("from todo t" +
                "   left join member m on t.writer_id = m.id");
        queryProvider.setWhereClause("where t.start_at >= now() + INTERVAL 30 minute" +
                " and t.start_at < now() + INTERVAL 33 minute" +
                " and t.is_all_day = 0");
        // 배치 수행 시간의 오차를 고려해서 3분의 오차 설정

        Map<String, Order> sortKeys = new HashMap<>();
        sortKeys.put("t.start_at", Order.ASCENDING);

        queryProvider.setSortKeys(sortKeys);

        return queryProvider.getObject();
    }

    @Bean
    @StepScope
    public ItemWriter<SendNotificationBatchDTO> sendNotificationWriter(MailService mailService) {
        return new SendNotificationWriter(mailService);
    }
}
