package com.divary.domain.logbase;

import com.divary.domain.member.entity.Member;
import com.divary.domain.logbase.logbook.enums.SaveStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LogBaseInfoRepository extends JpaRepository<LogBaseInfo, Long> {
    @Query("SELECT l FROM LogBaseInfo l WHERE YEAR(l.date) = :year AND l.saveStatus = :status AND l.member = :member ORDER BY l.date DESC")
    List<LogBaseInfo> findByYearAndStatusAndMember(@Param("year") int year, @Param("status") SaveStatus status, @Param("member") Member member);

    @Query("SELECT l FROM LogBaseInfo l WHERE YEAR(l.date) = :year AND l.member = :member ORDER BY l.date DESC")
    List<LogBaseInfo> findByYearAndMember(@Param("year") int year, @Param("member") Member member);

    Optional<LogBaseInfo> findByIdAndMemberId(Long id, Long memberId);

}
