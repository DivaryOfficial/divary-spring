package com.divary.domain.avatar.repository;


import com.divary.domain.Member.entity.Member;
import com.divary.domain.avatar.entity.Avatar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AvatarRepository extends JpaRepository<Avatar, Long> {
    Optional<Avatar> findByUser(Member user);
}
