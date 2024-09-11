package com.maruhxn.todomon.core.domain.todo.dao;

import com.maruhxn.todomon.core.domain.todo.domain.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    @Query("SELECT t from Todo t" +
            " WHERE t.writer.id = :writerId" +
            " AND t.startAt BETWEEN :startDate AND :endDate" +
            " AND t.repeatInfo IS NULL")
    List<Todo> findSingleTodosByWriterIdAndDate(@Param("writerId") long writerId,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);
}
