package com.divary.domain.notification.repository;

import com.divary.domain.Member.entity.Member;
import com.divary.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByReceiverId(Long Id);
}
