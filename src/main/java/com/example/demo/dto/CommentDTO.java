package com.example.demo.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentDTO {
    private Long commentId;
    private String content;
    private Long userId;
    private String authorName;
    private String authorMajor;
    private LocalDateTime createdAt;
    private Long parentId; // 부모 댓글 ID 추가

    public CommentDTO(Long commentId, String content, Long userId, String authorName, String authorMajor, LocalDateTime createdAt, Long parentId) {
        this.commentId = commentId;
        this.content = content;
        this.userId = userId;
        this.authorName = authorName;
        this.authorMajor = authorMajor;
        this.createdAt = createdAt;
        this.parentId = parentId; // 부모 ID 추가
    }
}
