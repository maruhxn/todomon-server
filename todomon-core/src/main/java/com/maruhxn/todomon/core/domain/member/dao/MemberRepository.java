package com.maruhxn.todomon.core.domain.member.dao;

import com.maruhxn.todomon.core.domain.member.domain.Member;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Member m")
    List<Member> findAllWithLock();

    Optional<Member> findByEmail(String email);

    @Query("SELECT m FROM Member m LEFT JOIN FETCH m.pets WHERE m.id = :memberId")
    Optional<Member> findMemberWithPets(Long memberId);

    Boolean existsByUsername(String username);

}
