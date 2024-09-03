package com.maruhxn.todomon.batch.chunk.processor;

import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.todo.domain.TodoAchievementHistory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CreateTodoAchievementHistoryProcessor implements ItemProcessor<Member, TodoAchievementHistory> {

    private final String date;

    // 생성자를 통해 date를 주입받음
    public CreateTodoAchievementHistoryProcessor(@Value("#{jobParameters['date']}") String date) {
        this.date = date;
    }

    @Override
    public TodoAchievementHistory process(Member member) throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return TodoAchievementHistory.builder()
                .member(member)
                .cnt(member.getDailyAchievementCnt())
                .date(LocalDate.parse(date, formatter))
                .build();
    }
}
