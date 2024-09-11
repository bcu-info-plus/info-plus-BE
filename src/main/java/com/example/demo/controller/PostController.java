package com.example.demo.controller;

import com.example.demo.dto.PostRequestDTO;
import com.example.demo.model.BoardType;
import com.example.demo.model.Post;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostService postService;

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
    public Post getPostById(@PathVariable int id) {
        return postService.findById(id);
    }

    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody PostRequestDTO postRequest) {
        try {
            // 서비스 레이어에서 비즈니스 로직 처리 및 저장
            Post savedPost = postService.savePost(postRequest);
            return ResponseEntity.ok(savedPost);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }

    @DeleteMapping("/{id}")
    public void deletePost(@PathVariable int id) {
        postService.delete(id);
    }
}
