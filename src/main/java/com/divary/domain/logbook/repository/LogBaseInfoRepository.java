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
    @Query("SELECT l FROM LogBaseInfo l WHERE YEAR(l.date) = :year AND l.saveStatus = :status AND l.member = :member ORDER BY l.date DESC")
    List<LogBaseInfo> findByYearAndStatusAndMember(@Param("year") int year, @Param("status") SaveStatus status, @Param("member") Member member);

    @Query("SELECT l FROM LogBaseInfo l WHERE YEAR(l.date) = :year AND l.member = :member ORDER BY l.date DESC")
    List<LogBaseInfo> findByYearAndMember(@Param("year") int year, @Param("member") Member member);

}
