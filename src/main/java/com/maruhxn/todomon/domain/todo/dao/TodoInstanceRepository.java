package com.maruhxn.todomon.domain.todo.dao;

import com.maruhxn.todomon.domain.todo.domain.TodoInstance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoInstanceRepository extends JpaRepository<TodoInstance, Long> {
    void deleteAllByTodo_Id(Long todoId);
}
