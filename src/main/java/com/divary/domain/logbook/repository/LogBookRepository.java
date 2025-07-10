package com.divary.domain.logbook.repository;

import com.divary.domain.logbook.entity.LogBook;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogBookRepository extends JpaRepository<LogBook,Long> {
}
