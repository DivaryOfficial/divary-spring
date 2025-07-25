package com.divary.domain.image.repository;

import com.divary.domain.image.entity.Image;
import com.divary.domain.image.entity.ImageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    
    // 사용자 ID로 이미지 목록 조회
    List<Image> findByUserId(Long userId);
    
    // 이미지 타입으로 이미지 목록 조회
    List<Image> findByType(ImageType type);
    
    // 사용자 ID와 이미지 타입으로 이미지 목록 조회
    List<Image> findByUserIdAndType(Long userId, ImageType type);
    
    // S3 키로 이미지 조회
    Image findByS3Key(String s3Key);
    
    // S3 키가 특정 패턴으로 시작하는 이미지 목록 조회
    List<Image> findByS3KeyStartingWith(String s3KeyPrefix);
} 