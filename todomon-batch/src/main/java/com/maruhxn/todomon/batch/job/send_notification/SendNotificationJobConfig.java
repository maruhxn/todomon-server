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
import org.springframework.batch.experimental.item.support.CompositeItemReader;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Arrays;
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

    @Bean
    @JobScope
    public Step sendNotificationStep(
            ItemReader<SendNotificationBatchDTO> sendNotificationReader,
            ItemProcessor<SendNotificationBatchDTO, SendNotificationBatchDTO> asyncSendNotificationProcessor,
            ItemWriter<SendNotificationBatchDTO> asyncSendNotificationWriter
    ) throws Exception {
        return new StepBuilder("sendNotificationStep", jobRepository)
                .<SendNotificationBatchDTO, SendNotificationBatchDTO>chunk(chunkSize, tx)
                .reader(sendNotificationReader)
                .processor(asyncSendNotificationProcessor)
                .writer(asyncSendNotificationWriter)
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    @StepScope
    public CompositeItemReader<SendNotificationBatchDTO> sendNotificationReader(
            JdbcPagingItemReader<SendNotificationBatchDTO> todoReader,
            JdbcPagingItemReader<SendNotificationBatchDTO> todoInstanceReader
    ) {
        return new CompositeItemReader<>(Arrays.asList(todoReader, todoInstanceReader));
    }

    @Bean
    @StepScope
    public JdbcPagingItemReader<SendNotificationBatchDTO> todoReader(
            PagingQueryProvider todoQueryProvider
    ) throws Exception {
        return new JdbcPagingItemReaderBuilder<SendNotificationBatchDTO>()
                .name("todoReader")
                .dataSource(this.dataSource)
                .pageSize(chunkSize)
                .beanRowMapper(SendNotificationBatchDTO.class)
                .queryProvider(todoQueryProvider)
                .build();
    }

    @Bean
    @StepScope
    public JdbcPagingItemReader<SendNotificationBatchDTO> todoInstanceReader(
            PagingQueryProvider todoInstanceQueryProvider
    ) throws Exception {
        return new JdbcPagingItemReaderBuilder<SendNotificationBatchDTO>()
                .name("todoInstanceReader")
                .dataSource(this.dataSource)
                .pageSize(chunkSize)
                .beanRowMapper(SendNotificationBatchDTO.class)
                .queryProvider(todoInstanceQueryProvider)
                .build();
    }

    @Bean
    public PagingQueryProvider todoQueryProvider() throws Exception {
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
    public PagingQueryProvider todoInstanceQueryProvider() throws Exception {
        SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
        queryProvider.setDataSource(dataSource);
        queryProvider.setSelectClause("ti.id, ti.start_at, ti.content, m.username, m.email");
        queryProvider.setFromClause("from todo_instance ti" +
                "   inner join todo t on ti.todo_id = t.id" +
                "   left join member m on t.writer_id = m.id");
        queryProvider.setWhereClause("where ti.start_at >= now() + INTERVAL 30 minute" +
                " and ti.start_at < now() + INTERVAL 33 minute" +
                " and ti.is_all_day = 0");

        Map<String, Order> sortKeys = new HashMap<>();
        sortKeys.put("ti.start_at", Order.ASCENDING);

        queryProvider.setSortKeys(sortKeys);

        return queryProvider.getObject();
    }

    @Bean
    @StepScope
    public ItemProcessor<SendNotificationBatchDTO, SendNotificationBatchDTO> sendNotificationProcessor() {
        return item -> {
            log.info("Send email to = {}", item.getEmail());
            return item;
        };
    }

    @Bean
    public AsyncItemProcessor<SendNotificationBatchDTO, SendNotificationBatchDTO> asyncSendNotificationProcessor() throws InterruptedException {
        AsyncItemProcessor<SendNotificationBatchDTO, SendNotificationBatchDTO> asyncItemProcessor = new AsyncItemProcessor<>();
        asyncItemProcessor.setDelegate(sendNotificationProcessor());
        asyncItemProcessor.setTaskExecutor(new SimpleAsyncTaskExecutor());

        return asyncItemProcessor;
    }

    @Bean
    @StepScope
    public ItemWriter<SendNotificationBatchDTO> sendNotificationWriter(MailService mailService) {
        return new SendNotificationWriter(mailService);
    }

    @Bean
    public AsyncItemWriter<SendNotificationBatchDTO> asyncSendNotificationWriter(MailService mailService) {

        AsyncItemWriter<SendNotificationBatchDTO> asyncItemWriter = new AsyncItemWriter<>();
        asyncItemWriter.setDelegate(sendNotificationWriter(mailService));

        return asyncItemWriter;
    }
}
