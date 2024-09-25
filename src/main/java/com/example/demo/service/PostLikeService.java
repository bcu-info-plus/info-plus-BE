package com.example.demo.service;

import com.example.demo.model.Post;
import com.example.demo.model.PostLike;
import com.example.demo.model.User;
import com.example.demo.repository.PostLikeRepository;
import com.example.demo.repository.PostRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;

    public PostLikeService(PostLikeRepository postLikeRepository, PostRepository postRepository) {
        this.postLikeRepository = postLikeRepository;
        this.postRepository = postRepository;
    }

    // 좋아요를 처리하는 메소드
    public boolean toggleLike(User user, Post post) {
        Optional<PostLike> existingLike = postLikeRepository.findByPostAndUser(post, user);

        // 사용자가 이미 좋아요를 눌렀으면 좋아요 취소
        if (existingLike.isPresent()) {
            postLikeRepository.delete(existingLike.get());
            return false; // 좋아요 취소
        } else {
            // 좋아요를 누르지 않았으면 새로운 PostLike 엔티티 생성
            PostLike newLike = new PostLike();
            newLike.setUser(user);
            newLike.setPost(post);
            postLikeRepository.save(newLike);
            return true; // 좋아요 추가
        }
    }

    public boolean isLiked(User user, Post post) {
        Optional<PostLike> existingLike = postLikeRepository.findByPostAndUser(post, user);

        // 사용자가 이미 좋아요를 눌렀으면
        if (existingLike.isPresent()) {
            return true; // true
        } else {
            // 좋아요를 누르지 않았으면
            return false; // false
        }
    }

    // 게시물의 좋아요 수 반환
    public Long getLikeCount(Post post) {
        return postLikeRepository.countByPost(post);
    }

    // 좋아요 추가
    public void likePost(Long postId, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // 좋아요 수 증가
        post.setLikesCount(post.getLikesCount() + 1);
        postRepository.save(post);

    }

    // 좋아요 취소
    public void unlikePost(Long postId, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // 좋아요 수 감소
        post.setLikesCount(post.getLikesCount() - 1);
        postRepository.save(post);

    }
}
