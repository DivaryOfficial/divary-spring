package com.divary.domain.Member.repository;

import com.divary.domain.Member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

<<<<<<< HEAD
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
=======
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {
>>>>>>> 25fb5bd (유저 엔티티 작성 및 repository 생성)
}
