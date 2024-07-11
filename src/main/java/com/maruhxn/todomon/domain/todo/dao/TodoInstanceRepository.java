package com.maruhxn.todomon.domain.todo.dao;

import com.maruhxn.todomon.domain.todo.domain.TodoInstance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TodoInstanceRepository extends JpaRepository<TodoInstance, Long> {

    List<TodoInstance> findAllByTodo_Id(Long todoId);

    void deleteAllByTodo_Id(Long todoId);
}
