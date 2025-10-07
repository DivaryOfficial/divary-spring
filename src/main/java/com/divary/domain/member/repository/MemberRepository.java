package com.divary.domain.member.repository;

import com.divary.domain.member.entity.Member;
import com.divary.domain.member.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    Optional<Member> findById(Long id);
    List<Member> findByStatusAndDeactivatedAtBefore(Status status, LocalDateTime cutoffDate);
}
