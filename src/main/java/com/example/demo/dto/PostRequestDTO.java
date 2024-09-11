package com.example.demo.dto;

import com.example.demo.model.Image;
import lombok.Data;

import java.util.Set;

@Data
public class PostRequestDTO {
    private String title;
    private String content;
    private String boardType;
    private Long userId; // 사용자 ID를 직접 받음
    private Set<Image> images;
}
