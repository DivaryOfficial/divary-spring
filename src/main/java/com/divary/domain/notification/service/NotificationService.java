package com.divary.domain.notification.service;

import com.divary.domain.Member.entity.Member;
import com.divary.domain.Member.service.MemberService;
import com.divary.domain.notification.dto.NotificationPatchRequestDTO;
import com.divary.domain.notification.dto.NotificationResponseDTO;
import com.divary.domain.notification.entity.Notification;
import com.divary.domain.notification.enums.NotificationType;
import com.divary.domain.notification.repository.NotificationRepository;
import com.divary.global.exception.BusinessException;
import com.divary.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final MemberService memberService;

    public List<NotificationResponseDTO> getNotification(Long userId) {


        Member receiver = memberService.findById(userId);

        List<Notification> notifications = notificationRepository.findByReceiver(receiver);

        if (notifications.isEmpty()) {
            throw new BusinessException(ErrorCode.NOTIFICAITION_NOT_FOUND);
        }

        return notifications.stream()
                .map(NotificationResponseDTO::from)
                .toList();

    }

    public void patchIsRead(Long userId, NotificationPatchRequestDTO patchRequestDTO) {
        Member receiver = memberService.findById(userId);

        Notification notification = notificationRepository.findByReceiverAndId(receiver, patchRequestDTO.getId());

        notification.setIsRead(true);

    }

    public Notification postTempNotoification(Long userId) {
        Member receiver = memberService.findById(userId);

        Notification notification = Notification.builder()
                .receiver(receiver)
                .message("임시 알림입니다.")
                .isRead(false)
                .type(NotificationType.SYSTEM) // 예: enum 값 TEMP
                .build();
        return notificationRepository.save(notification);
    }
}
