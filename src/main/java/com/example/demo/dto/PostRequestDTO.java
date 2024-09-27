package com.example.demo.dto;

import com.example.demo.model.Image;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Data
public class PostRequestDTO {
    private String title;
    private String content;
    private String boardType;
    private Long userId; // 사용자 ID를 직접 받음

    private Set<Long> existingImages;  // 기존 이미지 ID 리스트 (유지할 이미지들)
    // 실제 이미지 파일들은 MultipartFile로 받음
    private Set<MultipartFile> images;  // 새로 업로드할 이미지들

}
