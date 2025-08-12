package com.divary.domain.logbase.logbook.repository;

import com.divary.domain.logbase.logbook.enums.SaveStatus;
import com.divary.domain.member.entity.Member;
import com.divary.domain.logbase.LogBaseInfo;
import com.divary.domain.logbase.logbook.entity.LogBook;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LogBookRepository extends JpaRepository<LogBook,Long> {

    List<LogBook> findByLogBaseInfo(LogBaseInfo logBaseInfo);
    //로그베이스정보로 로그북들 찾기

    int countByLogBaseInfoMemberIdAndSaveStatus(Long memberId, SaveStatus saveStatus);

    int countByLogBaseInfo(LogBaseInfo logBaseInfo);

    Optional<LogBook> findByIdAndLogBaseInfoMemberId(Long logBookId, Long memberId);

}
