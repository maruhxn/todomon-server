package com.maruhxn.todomon.domain.todo.dao;

import com.maruhxn.todomon.domain.todo.domain.RepeatInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepeatInfoRepository extends JpaRepository<RepeatInfo, Long> {
}
