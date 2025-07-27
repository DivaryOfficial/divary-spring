package com.divary.domain.mypage.controller;

import com.divary.common.response.ApiResponse;
import com.divary.common.util.EnumValidator;
import com.divary.domain.chatroom.dto.response.ChatRoomMessageResponse;
import com.divary.domain.image.entity.ImageType;
import com.divary.domain.mypage.dto.requestDTO.MyPageImageRequestDTO;
import com.divary.domain.mypage.dto.requestDTO.MyPageLevelRequestDTO;
import com.divary.domain.mypage.dto.response.MyPageImageResponseDTO;
import com.divary.domain.mypage.service.MyPageService;
import com.divary.global.config.SwaggerConfig;
import com.divary.global.config.security.CustomUserPrincipal;
import com.divary.global.config.security.jwt.JwtTokenProvider;
import com.divary.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MyPageController {
    private final MyPageService myPageService;

    @PatchMapping("/level")
    @Transactional
    @SwaggerConfig.ApiSuccessResponse(dataType = Void.class)
    @SwaggerConfig.ApiErrorExamples(value = {ErrorCode.INVALID_INPUT_VALUE, ErrorCode.AUTHENTICATION_REQUIRED})
    public void updateLevel(@AuthenticationPrincipal CustomUserPrincipal userPrincipal, @RequestBody MyPageLevelRequestDTO requestDTO) {
        myPageService.updateLevel(userPrincipal.getId(), requestDTO);
    }

    @PostMapping(value ="/image/upload/{type}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SwaggerConfig.ApiSuccessResponse(dataType = MyPageImageResponseDTO.class)
    @SwaggerConfig.ApiErrorExamples(value = {ErrorCode.INVALID_INPUT_VALUE, ErrorCode.AUTHENTICATION_REQUIRED})
    public ApiResponse<MyPageImageResponseDTO> uploadImage(@AuthenticationPrincipal CustomUserPrincipal userPrincipal, @PathVariable("type") String type, @RequestPart("image") MultipartFile image) {

        ImageType imageType = EnumValidator.validateEnum(ImageType.class, type);

        MyPageImageResponseDTO response = myPageService.uploadImage(imageType, image, userPrincipal.getId());

        return ApiResponse.success(response);
    }
}
