package com.divary.domain.logbook.repository;

import com.divary.domain.logbook.entity.Companion;
import com.divary.domain.logbook.entity.LogBook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanionRepository extends JpaRepository<Companion, Long> {
    List<Companion> findByLogBook(LogBook logBook);

    void deleteByLogBook(LogBook logBook);

}

