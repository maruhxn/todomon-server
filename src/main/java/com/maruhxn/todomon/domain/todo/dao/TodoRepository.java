package com.maruhxn.todomon.domain.todo.dao;

import com.maruhxn.todomon.domain.todo.domain.Todo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoRepository extends JpaRepository<Todo, Long> {
}
