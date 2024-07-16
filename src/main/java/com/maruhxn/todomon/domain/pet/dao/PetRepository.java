package com.maruhxn.todomon.domain.pet.dao;

import com.maruhxn.todomon.domain.pet.domain.Pet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PetRepository extends JpaRepository<Pet, Long> {

    List<Pet> findAllByMember_Id(Long memberId);
}
