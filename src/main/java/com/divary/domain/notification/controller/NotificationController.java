package com.divary.domain.notification.controller;

import com.divary.common.response.ApiResponse;
import com.divary.domain.notification.dto.NotificationResponseDTO;
import com.divary.domain.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT") // jwt 필요시 작성
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    public ApiResponse<List<NotificationResponseDTO>> getNotification() {
        List<NotificationResponseDTO> response = notificationService.getNotification();
        return ApiResponse.success(response);
    }
}
