package com.example.demo.controller;

import com.example.demo.dto.CommentDTO;
import com.example.demo.model.Comment;
import com.example.demo.service.CommentService;
import com.example.demo.service.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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


    // 댓글 수정 API
    @PutMapping("/comments/{id}")
    public ResponseEntity<CommentDTO> updateComment(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        String newContent = request.get("content");

        CommentDTO updatedCommentDTO = commentService.updateComment(id, newContent, customUserDetails.getUser());

        return ResponseEntity.ok(updatedCommentDTO);  // 수정된 댓글 정보 반환
    }
}
