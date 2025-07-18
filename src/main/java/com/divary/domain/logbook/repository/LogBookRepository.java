package com.divary.domain.logbook.repository;

import com.divary.domain.Member.entity.Member;
import com.divary.domain.logbook.entity.LogBaseInfo;
import com.divary.domain.logbook.entity.LogBook;
import com.divary.domain.logbook.enums.SaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LogBookRepository extends JpaRepository<LogBook,Long> {

    List<LogBook> findByLogBaseInfo(LogBaseInfo logBaseInfo);
    //로그베이스정보로 로그북들 찾기

    int countByLogBaseInfoMember(Member member);

    int countByLogBaseInfo(LogBaseInfo logBaseInfo);
}
