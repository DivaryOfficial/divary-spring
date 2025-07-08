package com.divary.domain.chatroom.repository;

import com.divary.domain.chatroom.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    // 공개 ID로 채팅방 조회
    Optional<ChatRoom> findByPublicId(String publicId);

    // 사용자 ID로 채팅방 목록 조회
    List<ChatRoom> findByUserIdOrderByCreatedAtDesc(Long userId);

    // 공개 ID 존재 여부 확인
    boolean existsByPublicId(String publicId);
}