package com.divary.domain.notification.repository;

import com.divary.domain.member.entity.Member;
import com.divary.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByReceiverId(Long id);
    Optional<Notification> findByReceiverIdAndId(Long receiverId, Long notificationId);
}
