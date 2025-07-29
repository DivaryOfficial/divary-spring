package com.divary.domain.diary.repository;

import com.divary.domain.diary.entity.Diary;
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

    @Query("""
                SELECT d FROM Diary d
                WHERE d.logBaseInfo.id = :logBaseInfoId
                AND d.logBaseInfo.member.id = :memberId
            """)
    Optional<Diary> findByLogBaseInfoIdAndMemberId(@Param("logBaseInfoId") Long logBaseInfoId,
                                                   @Param("memberId") Long memberId);

}
