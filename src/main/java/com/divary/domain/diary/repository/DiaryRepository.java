package com.divary.domain.diary.repository;

import com.divary.domain.diary.entity.Diary;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiaryRepository extends JpaRepository<Diary, Long> {
    @EntityGraph(attributePaths = {
            "logBook",
            "logBook.logBaseInfo",
            "logBook.logBaseInfo.member"
    })
    Optional<Diary> findByLogBookId(Long logId);
    boolean existsByLogBookId(Long logId);
}
