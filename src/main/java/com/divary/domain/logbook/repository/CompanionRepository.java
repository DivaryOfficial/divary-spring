package com.divary.domain.logbook.repository;

import com.divary.domain.logbook.entity.Companion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanionRepository extends JpaRepository<Companion, Long> {
    List<Companion> findByLogBookId(Long logBookId);
}

