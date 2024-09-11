package com.maruhxn.todomon.core.domain.member.dao;

import com.maruhxn.todomon.core.domain.member.domain.TitleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TitleNameRepository extends JpaRepository<TitleName, Long> {
    Optional<TitleName> findByMember_Id(Long memberId);
}
