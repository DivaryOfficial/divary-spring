package com.divary.domain.avatar.controller;

import com.divary.common.response.ApiResponse;
import com.divary.domain.avatar.dto.AvatarRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/avatar")
public class AvatarController {
    @PostMapping
    public ApiResponse<String> saveAvatar(@RequestBody AvatarRequestDTO avatarRequestDTO) {

        return ApiResponse.success("아바타 저장에 성공했습니다.", null);
    }
}
