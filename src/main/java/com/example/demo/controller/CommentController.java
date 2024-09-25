package com.example.demo.controller;

import com.example.demo.dto.CommentDTO;
import com.example.demo.model.Comment;
import com.example.demo.service.CommentService;
import com.example.demo.service.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/post/{postId}")
    public ResponseEntity<CommentDTO> createComment(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody CommentDTO commentRequest) {

        Long userId = customUserDetails.getId();
        String content = commentRequest.getContent();
        Long parentId = commentRequest.getParentId(); // parentId가 null일 수 있음

        CommentDTO newComment = commentService.createComment(postId, userId, content, parentId);
        return ResponseEntity.ok(newComment);
    }

    // 특정 게시물에 대한 댓글 조회
    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentDTO>> getCommentsByPost(@PathVariable Long postId) {
        List<CommentDTO> comments = commentService.getCommentsByPost(postId);
        return ResponseEntity.ok(comments);
    }

    // 댓글 삭제 (isDeleted 상태 변경)
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        Long userId = customUserDetails.getId();
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }

    // 특정 게시글의 댓글 개수를 조회하는 API
    @GetMapping("/post/{postId}/count")
    public ResponseEntity<Long> getCommentCountByPost(@PathVariable Long postId) {
        Long count = commentService.countCommentsByPostId(postId);
        return ResponseEntity.ok(count);
    }
}
