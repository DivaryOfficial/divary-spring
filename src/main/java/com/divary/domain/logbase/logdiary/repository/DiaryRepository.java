package com.divary.domain.logbase.logdiary.repository;

import com.divary.domain.logbase.logdiary.entity.Diary;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DiaryRepository extends JpaRepository<Diary, Long> {
    @EntityGraph(attributePaths = {
            "logBaseInfo",
            "logBaseInfo.member"
    })
    boolean existsByLogBaseInfoId(Long logBaseInfoId);

    @Query("SELECT d FROM Diary d WHERE d.logBaseInfo.id = :logBaseInfoId")
    Optional<Diary> findByLogBaseInfoId(@Param("logBaseInfoId") Long logBaseInfoId);

}
