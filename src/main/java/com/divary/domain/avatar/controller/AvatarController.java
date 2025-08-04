package com.divary.domain.avatar.controller;

import com.divary.common.response.ApiResponse;
import com.divary.domain.avatar.dto.AvatarRequestDTO;
import com.divary.domain.avatar.dto.AvatarResponseDTO;
import com.divary.domain.avatar.entity.Avatar;
import com.divary.domain.avatar.service.AvatarService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/avatar")
public class AvatarController {
    private final AvatarService avatarService;

    @PatchMapping
    public ApiResponse<String> saveAvatar(@RequestBody @Valid AvatarRequestDTO avatarRequestDTO) {
        avatarService.patchAvatar(avatarRequestDTO);
        return ApiResponse.success("아바타 저장에 성공했습니다.", null);
    }

    @GetMapping
    public  ApiResponse<AvatarResponseDTO> getAvatar(){
        return ApiResponse.success("아바타 조회에 성공했습니다.", avatarService.getAvatar());
    }
}
