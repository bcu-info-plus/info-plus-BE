package com.example.demo.repository;

import com.example.demo.model.Post;
import com.example.demo.model.PostLike;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    // 게시글과 사용자를 기반으로 좋아요 상태 조회
    Optional<PostLike> findByPostAndUser(Post post, User user);

    // 게시글과 사용자를 기반으로 좋아요가 있는지 여부를 확인
    boolean existsByPostAndUser(Post post, User user);

    // 특정 게시물의 좋아요 수 계산
    Long countByPost(Post post);
}