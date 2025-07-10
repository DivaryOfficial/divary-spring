package com.divary.domain.encyclopedia.repository;

import com.divary.domain.encyclopedia.entity.EncyclopediaCard;
import com.divary.domain.encyclopedia.enums.Type;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EncyclopediaCardRepository extends JpaRepository<EncyclopediaCard, Long> {
    List<EncyclopediaCard> findAllByType(Type type);
}
