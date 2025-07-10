package com.divary.domain.chatroom.repository;

import com.divary.domain.chatroom.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    // 사용자 ID로 채팅방 목록 조회
    // TODO : 추후 채팅방 조회 시 정렬 순서 파라미터 추가 필요
    List<ChatRoom> findByUserIdOrderByCreatedAtDesc(Long userId);

}