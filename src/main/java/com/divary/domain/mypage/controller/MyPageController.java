package com.divary.domain.mypage.controller;

import com.divary.common.response.ApiResponse;
import com.divary.common.util.EnumValidator;
import com.divary.domain.chatroom.dto.response.ChatRoomMessageResponse;
import com.divary.domain.image.entity.ImageType;
import com.divary.domain.mypage.dto.requestDTO.MyPageImageRequestDTO;
import com.divary.domain.mypage.dto.requestDTO.MyPageLevelRequestDTO;
import com.divary.domain.mypage.dto.response.MyPageImageResponseDTO;
import com.divary.domain.mypage.service.MyPageService;
import com.divary.global.config.SwaggerConfig.ApiSuccessResponse;
import com.divary.global.config.SwaggerConfig.ApiErrorExamples;
import com.divary.global.config.security.CustomUserPrincipal;
import com.divary.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MyPageController {
    private final MyPageService myPageService;

    @PatchMapping("/level")
    @ApiSuccessResponse(dataType = Void.class)
    @ApiErrorExamples(value = {ErrorCode.INVALID_INPUT_VALUE, ErrorCode.AUTHENTICATION_REQUIRED})
    public ApiResponse updateLevel(@AuthenticationPrincipal CustomUserPrincipal userPrincipal, @Valid @RequestBody MyPageLevelRequestDTO requestDTO) {
        myPageService.updateLevel(userPrincipal.getId(), requestDTO);
        return ApiResponse.success(null);
    }

    @PostMapping(value ="/image/upload/{type}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiSuccessResponse(dataType = MyPageImageResponseDTO.class)
    @ApiErrorExamples(value = {ErrorCode.INVALID_INPUT_VALUE, ErrorCode.AUTHENTICATION_REQUIRED})
    public ApiResponse<MyPageImageResponseDTO> uploadImage(@AuthenticationPrincipal CustomUserPrincipal userPrincipal, @PathVariable("type") String type, @Valid @ModelAttribute MyPageImageRequestDTO requestDTO) {

        ImageType imageType = EnumValidator.validateEnum(ImageType.class, type);

        MyPageImageResponseDTO response = myPageService.uploadImage(imageType, requestDTO, userPrincipal.getId());

        return ApiResponse.success(response);
    }
}
