package com.maruhxn.todomon.batch.job.todo_achievement;

import com.maruhxn.todomon.batch.chunk.processor.CreateTodoAchievementHistoryProcessor;
import com.maruhxn.todomon.batch.chunk.writer.CachingDTOItemWriter;
import com.maruhxn.todomon.batch.listener.MemberStepListener;
import com.maruhxn.todomon.batch.listener.StopWatchJobListener;
import com.maruhxn.todomon.batch.rowmapper.MemberAchievementRowMapper;
import com.maruhxn.todomon.batch.validator.DateParameterValidator;
import com.maruhxn.todomon.batch.vo.MemberAchievementDTO;
import com.maruhxn.todomon.core.domain.todo.domain.TodoAchievementHistory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.*;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DailyTodoAchievementJobConfig {

    private static final int CHUNK_SIZE = 100;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager tx;
    private final DataSource dataSource;

    @Bean
    public Job dailyTodoAchievementJob(
            Step updateMemberStep,
            Step createTodoAchievementHistoryStep
    ) throws Exception {
        return new JobBuilder("dailyTodoAchievementJob", jobRepository)
                .validator(new DateParameterValidator())
                .incrementer(new RunIdIncrementer())
                .start(updateMemberStep)
                .next(createTodoAchievementHistoryStep)
                .listener(new StopWatchJobListener())
                .build();
    }

    @Bean
    @JobScope
    public Step updateMemberStep(
            ItemReader<MemberAchievementDTO> memberItemReader,
            CompositeItemWriter<MemberAchievementDTO> memberUpdateWriter
    ) throws Exception {
        return new StepBuilder("updateMemberStep", jobRepository)
                .<MemberAchievementDTO, MemberAchievementDTO>chunk(CHUNK_SIZE, tx) // 한 번에 처리할 청크 크기
                .reader(memberItemReader)
                .writer(memberUpdateWriter)
                .listener(new MemberStepListener(processedMembers()))
                .build();
    }

    @Bean
    @JobScope
    public Step createTodoAchievementHistoryStep(
            ItemReader<MemberAchievementDTO> cachedMemberReader,
            ItemProcessor<MemberAchievementDTO, TodoAchievementHistory> createTodoAchievementHistoryProcessor,
            ItemWriter<TodoAchievementHistory> todoAchievementHistoryItemWriter
    ) throws Exception {
        return new StepBuilder("createTodoAchievementHistoryStep", jobRepository)
                .<MemberAchievementDTO, TodoAchievementHistory>chunk(CHUNK_SIZE, tx) // 한 번에 처리할 청크 크기
                .reader(cachedMemberReader)
                .processor(createTodoAchievementHistoryProcessor)
                .writer(todoAchievementHistoryItemWriter)
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<MemberAchievementDTO> cachedMemberReader(
            @Value("#{jobExecutionContext['processedMembers']}") List<MemberAchievementDTO> processedMemberAchievements) {
        return new ListItemReader<>(processedMemberAchievements);
    }

    @Bean
    @StepScope
    public JdbcPagingItemReader<MemberAchievementDTO> memberItemReader(
            PagingQueryProvider queryProvider
    ) throws Exception {
        JdbcPagingItemReader<MemberAchievementDTO> reader = new JdbcPagingItemReader<>() {
            @Override
            public int getPage() {
                return 0;
            }
        };

        reader.setName("memberItemReader");
        reader.setPageSize(CHUNK_SIZE);
        reader.setDataSource(dataSource);
        reader.setRowMapper(new MemberAchievementRowMapper());
        reader.setQueryProvider(queryProvider);

        return reader;
    }

    @Bean
    public PagingQueryProvider queryProvider() throws Exception {
        SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
        queryProvider.setDataSource(dataSource);
        queryProvider.setSelectClause("id, daily_achievement_cnt");
        queryProvider.setFromClause("from member");
        queryProvider.setWhereClause("where daily_achievement_cnt > 0");

        Map<String, Order> sortKeys = new HashMap<>();
        sortKeys.put("id", Order.ASCENDING);

        queryProvider.setSortKeys(sortKeys);

        return queryProvider.getObject();
    }

    @Bean
    @StepScope
    public CompositeItemWriter<MemberAchievementDTO> memberUpdateWriter() {
        JdbcBatchItemWriter<MemberAchievementDTO> jdbcBatchItemWriter = new JdbcBatchItemWriterBuilder<MemberAchievementDTO>()
                .dataSource(dataSource)
                .sql("UPDATE member " +
                        "SET star_point = star_point + scheduled_reward, " +
                        "daily_achievement_cnt = 0, " +
                        "scheduled_reward = 0 " +
                        "WHERE id = ?")
                .itemPreparedStatementSetter((item, ps) -> {
                    ps.setLong(1, item.getMemberId());
                })
                .build();

        CompositeItemWriter<MemberAchievementDTO> compositeItemWriter = new CompositeItemWriter<>();
        compositeItemWriter.setDelegates(Arrays.asList(cachingDtoItemWriter(), jdbcBatchItemWriter));

        return compositeItemWriter;
    }

    @Bean
    @StepScope
    public ItemWriter<MemberAchievementDTO> cachingDtoItemWriter() {
        return new CachingDTOItemWriter(processedMembers());
    }

    @Bean
    @StepScope
    public CreateTodoAchievementHistoryProcessor createTodoAchievementHistoryProcessor(
            @Value("#{jobParameters['date']}") String date) {
        return new CreateTodoAchievementHistoryProcessor(date);
    }

    @Bean
    @StepScope
    public JdbcBatchItemWriter<TodoAchievementHistory> todoAchievementHistoryItemWriter() {
        return new JdbcBatchItemWriterBuilder<TodoAchievementHistory>()
                .dataSource(dataSource)
                .sql("INSERT INTO todo_achievement_history (member_id, cnt, date, created_at, updated_at) VALUES (:memberId, :cnt, :date, now(), now())")
                .beanMapped()
                .build();
    }

    @Bean
    public List<MemberAchievementDTO> processedMembers() {
        return new ArrayList<>();
    }
}
