package com.divary.domain.logbase.logbook.repository;

import com.divary.domain.logbase.logbook.entity.Companion;
import com.divary.domain.logbase.logbook.entity.LogBook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanionRepository extends JpaRepository<Companion, Long> {
    List<Companion> findByLogBook(LogBook logBook);

    void deleteByLogBook(LogBook logBook);

}

