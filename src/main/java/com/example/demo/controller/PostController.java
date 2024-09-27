package com.example.demo.controller;

import com.example.demo.dto.PostRequestDTO;
import com.example.demo.model.BoardType;
import com.example.demo.model.Post;
import com.example.demo.model.User;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.CustomUserDetails;
import com.example.demo.service.PostLikeService;
import com.example.demo.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostService postService;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private PostLikeService postLikeService;

    @GetMapping
    public ResponseEntity<List<Post>> getAllPosts(@RequestParam(required = false) String boardType) {
        try {
            List<Post> posts = postService.getPostsByBoardType(boardType);
            return ResponseEntity.ok(posts);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.emptyList());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @GetMapping("/{id}")
    public Post getPostById(@PathVariable Long id) {
        return postService.findById(id);
    }

    @PostMapping
    public ResponseEntity<?> createPost(@ModelAttribute PostRequestDTO postRequest) {
        System.out.println("Request received: " + postRequest.getTitle());

        try {
            // 서비스 레이어에서 비즈니스 로직 처리 및 저장
            Post savedPost = postService.savePost(postRequest);
            System.out.println("Post saved with ID: " + savedPost.getPostId());
            return ResponseEntity.ok(savedPost);
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }

    // 게시글 삭제 (soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        User user = customUserDetails.getUser();  // 현재 로그인한 사용자의 정보 가져오기

        try {
            // 게시글 삭제 요청
            postService.deletePost(id, user);
            return ResponseEntity.ok().build();  // 성공적으로 삭제되었을 경우 200 OK 반환
        } catch (RuntimeException e) {
            // 권한이 없거나 게시글이 없는 경우 403 Forbidden 또는 404 Not Found 처리
            return ResponseEntity.status(403).build();  // 403 Forbidden
        }
    }

    // 게시글 수정
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePost(
            @PathVariable Long id,
            @ModelAttribute PostRequestDTO postRequest,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        System.out.println("Update request received for post ID: " + id + ", title: " + postRequest.getTitle());

        // 현재 로그인한 사용자의 User 객체 가져오기
        User user = customUserDetails.getUser();

        try {
            // 게시글 수정 요청
            Post updatedPost = postService.updatePost(id, postRequest, user);
            System.out.println("Post updated with ID: " + updatedPost.getPostId());
            return ResponseEntity.ok(updatedPost);  // 성공적으로 수정된 게시글 반환
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }

    // 좋아요 토글 API
    @PostMapping("/{postId}/like")
    public String toggleLike(@PathVariable Long postId, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("Post not found"));
        boolean liked = postLikeService.toggleLike(customUserDetails.getUser(), post);

        if (liked) {
            postLikeService.likePost(postId, customUserDetails.getUser());
            return "좋아요가 추가되었습니다.";
        } else {
            postLikeService.unlikePost(postId, customUserDetails.getUser());
            return "좋아요가 취소되었습니다.";
        }
    }

    // 게시물의 좋아요 수 조회 API
    @GetMapping("/{postId}/likes")
    public Long getLikeCount(@PathVariable Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("Post not found"));
        return postLikeService.getLikeCount(post);
    }

    // 게시물 좋아요 눌렀는지 확인 API
    @GetMapping("/{postId}/isliked")
    public boolean checkIsLiked(@PathVariable Long postId, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("Post not found"));
        return postLikeService.isLiked(customUserDetails.getUser(), post);
    }
}
