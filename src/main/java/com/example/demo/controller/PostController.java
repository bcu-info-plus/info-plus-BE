package com.example.demo.controller;

import com.example.demo.model.Post;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostService postService;
    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<Post> getAllPosts() {
        return postService.findAll();
    }

    @GetMapping("/{id}")
    public Post getPostById(@PathVariable int id) {
        return postService.findById(id);
    }

    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody Post post) {
        // Check if the user already exists
        User user = post.getUser();
        if (user.getUserId() == 0) {
            // If the user is new (userId == 0), save the user first
            user = userRepository.save(user); // userRepository를 주입받아 사용해야 함
            post.setUser(user);
        }

        Post savedPost = postService.save(post); // Post 엔티티 저장
        return ResponseEntity.ok(savedPost);
    }

    @DeleteMapping("/{id}")
    public void deletePost(@PathVariable int id) {
        postService.delete(id);
    }
}
