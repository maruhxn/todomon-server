package com.maruhxn.todomon.core.domain.member.dao;

import com.maruhxn.todomon.core.domain.member.domain.Member;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Member m JOIN FETCH m.diligence WHERE m.id = :memberId")
    Optional<Member> findMemberWithDiligenceUsingLock(Long memberId);

    Optional<Member> findByEmail(String email);

    @Query("SELECT m FROM Member m LEFT JOIN FETCH m.pets WHERE m.id = :memberId")
    Optional<Member> findMemberWithPets(Long memberId);

    @Query("SELECT m FROM Member m LEFT JOIN FETCH m.representPet WHERE m.id = :memberId")
    Optional<Member> findMemberWithRepresentPet(Long memberId);
}
