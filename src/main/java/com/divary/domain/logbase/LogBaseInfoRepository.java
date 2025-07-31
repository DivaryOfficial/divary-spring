package com.divary.domain.logbase;

import com.divary.domain.logbase.logbook.enums.SaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LogBaseInfoRepository extends JpaRepository<LogBaseInfo, Long> {
    @Query("SELECT l FROM LogBaseInfo l WHERE YEAR(l.date) = :year AND l.saveStatus = :status ORDER BY l.date DESC")
    List<LogBaseInfo> findByYearAndStatus(@Param("year") int year, @Param("status") SaveStatus status);

    @Query("SELECT l FROM LogBaseInfo l WHERE YEAR(l.date) = :year ORDER BY l.date DESC")
    List<LogBaseInfo> findByYear(@Param("year") int year);

}
