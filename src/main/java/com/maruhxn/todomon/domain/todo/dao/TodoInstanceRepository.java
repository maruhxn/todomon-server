package com.maruhxn.todomon.domain.todo.dao;

import com.maruhxn.todomon.domain.todo.domain.TodoInstance;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TodoInstanceRepository extends JpaRepository<TodoInstance, Long> {

    List<TodoInstance> findAllByTodo_Id(Long todoId);

    void deleteAllByTodo_Id(Long todoId);

    @Query("SELECT ti from TodoInstance ti WHERE ti.todo.writer.id = :writerId" +
            " AND ti.startAt BETWEEN :startDate AND :endDate")
    @EntityGraph(attributePaths = {"todo"})
    List<TodoInstance> findAllByWriterIdAndDate(@Param("writerId") long writerId,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);
}
