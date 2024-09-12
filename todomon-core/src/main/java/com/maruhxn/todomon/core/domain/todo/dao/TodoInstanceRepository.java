package com.maruhxn.todomon.core.domain.todo.dao;

import com.maruhxn.todomon.core.domain.todo.domain.TodoInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TodoInstanceRepository extends JpaRepository<TodoInstance, Long> {

    List<TodoInstance> findAllByTodo_Id(Long todoId);

    @Query("SELECT ti from TodoInstance ti JOIN FETCH ti.todo t WHERE ti.id = :instanceId")
    Optional<TodoInstance> findTodoInstanceWithTodo(Long instanceId);

    void deleteAllByTodo_Id(Long todoId);

    @Query("SELECT DISTINCT ti from TodoInstance ti" +
            " JOIN FETCH ti.todo t" +
            " JOIN FETCH t.repeatInfo" +
            " WHERE t.writer.id = :writerId" +
            " AND ti.startAt BETWEEN :startDate AND :endDate")
    List<TodoInstance> findAllByWriterIdAndDate(@Param("writerId") long writerId,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);
}
