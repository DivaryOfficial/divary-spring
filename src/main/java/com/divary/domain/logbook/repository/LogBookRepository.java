package com.divary.domain.logbook.repository;

import com.divary.domain.Member.entity.Member;
import com.divary.domain.logbook.entity.LogBook;
import com.divary.domain.logbook.enums.saveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LogBookRepository extends JpaRepository<LogBook,Long> {

    @Query("SELECT l FROM LogBook l WHERE YEAR(l.date) = :year AND l.saveStatus = :status ORDER BY l.date DESC")
    List<LogBook> findByYearAndStatus(@Param("year") int year, @Param("status") saveStatus status);

    int countByMember(Member member);
    //로그북 누적횟수 세기

}
