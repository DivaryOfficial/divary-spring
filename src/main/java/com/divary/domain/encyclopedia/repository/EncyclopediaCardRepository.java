package com.divary.domain.encyclopedia.repository;

import com.divary.domain.encyclopedia.entity.EncyclopediaCard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EncyclopediaCardRepository extends JpaRepository<EncyclopediaCard, Long> {
}
