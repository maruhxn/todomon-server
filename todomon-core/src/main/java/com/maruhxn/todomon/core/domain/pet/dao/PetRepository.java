package com.maruhxn.todomon.core.domain.pet.dao;

import com.maruhxn.todomon.core.domain.pet.domain.Pet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PetRepository extends JpaRepository<Pet, Long> {

    Optional<Pet> findOneByIdAndMember_Id(Long petId, Long memberId);

    List<Pet> findAllByMember_Id(Long memberId);
}
