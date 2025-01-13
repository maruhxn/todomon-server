package com.maruhxn.todomon.core.domain.pet.dao;

import com.maruhxn.todomon.core.domain.pet.domain.CollectedPet;
import com.maruhxn.todomon.core.domain.pet.domain.Rarity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CollectedPetRepository extends JpaRepository<CollectedPet, Long> {
    boolean existsByMember_IdAndRarityAndAppearance(Long memberId, Rarity rarity, String appearance);

    List<CollectedPet> findAllByMember_Id(Long memberId);
}
