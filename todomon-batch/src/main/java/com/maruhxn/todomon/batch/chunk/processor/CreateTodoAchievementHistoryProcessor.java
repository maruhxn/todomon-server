package com.maruhxn.todomon.batch.chunk.processor;

import com.maruhxn.todomon.batch.vo.MemberAchievementDTO;
import com.maruhxn.todomon.core.domain.todo.domain.TodoAchievementHistory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CreateTodoAchievementHistoryProcessor implements ItemProcessor<MemberAchievementDTO, TodoAchievementHistory> {

    private final String date;

    // 생성자를 통해 date를 주입받음
    public CreateTodoAchievementHistoryProcessor(@Value("#{jobParameters['date']}") String date) {
        this.date = date;
    }

    @Override
    public TodoAchievementHistory process(MemberAchievementDTO item) throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return TodoAchievementHistory.builder()
                .memberId(item.getMemberId())
                .cnt(item.getAchievementCount())
                .date(LocalDate.parse(date, formatter))
                .build();
    }
}
