package com.maruhxn.todomon.domain.pet.dao;

import com.maruhxn.todomon.domain.pet.domain.CollectedPet;
import com.maruhxn.todomon.domain.pet.domain.Rarity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CollectedPetRepository extends JpaRepository<CollectedPet, Long> {
    Optional<CollectedPet> findByMember_IdAndRarityAndAppearance(Long memberId, Rarity rarity, String appearance);

    List<CollectedPet> findAllByMember_Id(Long memberId);
}
