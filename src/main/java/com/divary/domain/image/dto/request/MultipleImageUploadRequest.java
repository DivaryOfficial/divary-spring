package com.divary.domain.image.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Builder
@Schema(description = "다중 이미지 업로드 요청")
public class MultipleImageUploadRequest {
    
    @Schema(description = "업로드할 이미지 파일들", required = true)
    private List<MultipartFile> files;
    
    public MultipleImageUploadRequest(List<MultipartFile> files) {
        this.files = files;
    }
}