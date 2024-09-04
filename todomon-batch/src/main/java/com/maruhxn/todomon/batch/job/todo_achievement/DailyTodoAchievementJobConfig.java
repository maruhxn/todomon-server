package com.maruhxn.todomon.batch.job.todo_achievement;

import com.maruhxn.todomon.batch.chunk.processor.CreateTodoAchievementHistoryProcessor;
import com.maruhxn.todomon.batch.chunk.writer.MemberUpdateWriter;
import com.maruhxn.todomon.batch.listener.MemberStepListener;
import com.maruhxn.todomon.batch.service.MemberStateUpdateService;
import com.maruhxn.todomon.batch.validator.DateParameterValidator;
import com.maruhxn.todomon.batch.vo.MemberAchievementDTO;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.todo.domain.TodoAchievementHistory;
import jakarta.persistence.EntityManagerFactory;
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
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DailyTodoAchievementJobConfig {

    private static final int CHUNK_SIZE = 100;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager tx;
    private final EntityManagerFactory entityManagerFactory;

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
                .build();
    }

    @Bean
    @JobScope
    public Step updateMemberStep(
            ItemReader<Member> memberItemReader,
            ItemWriter<Member> memberUpdateWriter
    ) throws Exception {
        return new StepBuilder("updateMemberStep", jobRepository)
                .<Member, Member>chunk(CHUNK_SIZE, tx) // 한 번에 처리할 청크 크기
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
    public JpaPagingItemReader<Member> memberItemReader() throws Exception {
        JpaPagingItemReader<Member> reader = new JpaPagingItemReader<>() {
            @Override
            public int getPage() {
                return 0;
            }
        };

        reader.setName("memberItemReader");
        reader.setPageSize(CHUNK_SIZE);
        reader.setEntityManagerFactory(entityManagerFactory);
        reader.setQueryString("select m from Member m WHERE m.dailyAchievementCnt > 0");

        return reader;
    }

    @Bean
    @StepScope
    public ItemWriter<Member> memberUpdateWriter(MemberStateUpdateService memberStateUpdateService, List<MemberAchievementDTO> processedMembers) {
        return new MemberUpdateWriter(processedMembers, memberStateUpdateService);
    }

    @Bean
    @StepScope
    public CreateTodoAchievementHistoryProcessor createTodoAchievementHistoryProcessor(
            @Value("#{jobParameters['date']}") String date) {
        return new CreateTodoAchievementHistoryProcessor(date);
    }

    @Bean
    @StepScope
    public JpaItemWriter<TodoAchievementHistory> todoAchievementHistoryItemWriter() {
        return new JpaItemWriterBuilder<TodoAchievementHistory>()
                .usePersist(true)
                .entityManagerFactory(entityManagerFactory)
                .build();
    }

    @Bean
    public List<MemberAchievementDTO> processedMembers() {
        return new ArrayList<>();
    }
}
