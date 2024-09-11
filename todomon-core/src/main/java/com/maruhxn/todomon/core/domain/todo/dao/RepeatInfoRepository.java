package com.maruhxn.todomon.core.domain.todo.dao;

import com.maruhxn.todomon.core.domain.todo.domain.RepeatInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepeatInfoRepository extends JpaRepository<RepeatInfo, Long> {
}
