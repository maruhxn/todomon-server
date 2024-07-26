package com.maruhxn.todomon.global.schedule;

import com.maruhxn.todomon.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.domain.member.domain.Member;
import com.maruhxn.todomon.domain.todo.dao.TodoAchievementHistoryRepository;
import com.maruhxn.todomon.domain.todo.domain.TodoAchievementHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ScheduleService {

    private final MemberRepository memberRepository;
    private final TodoAchievementHistoryRepository todoAchievementHistoryRepository;

    @Scheduled(cron = "0 0 0 * * *") // 매일 00시에 실행
    @Transactional
    public void saveDailyAchievementAndReset() {
        List<Member> members = memberRepository.findAllWithLock();

        LocalDate yesterday = LocalDate.now().minusDays(1);

        for (Member member : members) {
            TodoAchievementHistory history = TodoAchievementHistory.builder()
                    .cnt(member.getDailyAchievementCnt())
                    .date(yesterday)
                    .member(member)
                    .build();
            todoAchievementHistoryRepository.save(history);

            member.resetDailyAchievement();
        }

        memberRepository.saveAll(members);
    }
}