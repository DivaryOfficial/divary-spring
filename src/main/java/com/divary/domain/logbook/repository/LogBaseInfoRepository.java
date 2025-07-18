package com.divary.domain.logbook.repository;

import com.divary.domain.Member.entity.Member;
import com.divary.domain.logbook.entity.LogBaseInfo;
import com.divary.domain.logbook.enums.SaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LogBaseInfoRepository extends JpaRepository<LogBaseInfo, Long> {
    @Query("SELECT l FROM LogBaseInfo l WHERE YEAR(l.date) = :year AND l.saveStatus = :status ORDER BY l.date DESC")
    List<LogBaseInfo> findByYearAndStatus(@Param("year") int year, @Param("status") SaveStatus status);

    @Query("SELECT l FROM LogBaseInfo l WHERE YEAR(l.date) = :year ORDER BY l.date DESC")
    List<LogBaseInfo> findByYear(@Param("year") int year);

    Optional<LogBaseInfo> findByDate(LocalDate date);

}
