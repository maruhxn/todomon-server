package com.maruhxn.todomon.core.domain.pet.dao;

import com.maruhxn.todomon.core.domain.pet.domain.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PetRepository extends JpaRepository<Pet, Long> {

    Optional<Pet> findOneByIdAndMember_Id(Long petId, Long memberId);


    @Query("SELECT p FROM Pet p JOIN FETCH p.member WHERE p.id = :petId")
    Optional<Pet> findOneByIdWithMember(Long petId);

    List<Pet> findAllByMember_Id(Long memberId);
}
