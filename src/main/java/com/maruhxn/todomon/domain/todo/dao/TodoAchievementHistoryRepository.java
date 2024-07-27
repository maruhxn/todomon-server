package com.maruhxn.todomon.domain.todo.dao;

import com.maruhxn.todomon.domain.todo.domain.TodoAchievementHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoAchievementHistoryRepository extends JpaRepository<TodoAchievementHistory, Long> {
}
