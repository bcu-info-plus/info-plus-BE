package com.example.demo.repository;

import com.example.demo.model.Comment;
import com.example.demo.model.IsDeleted;
import com.example.demo.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    // Post와 IsDeleted를 기준으로 댓글 조회
    List<Comment> findAllByPostAndIsDeleted(Post post, IsDeleted isDeleted); // Post 객체로 조회

    // 특정 게시글의 댓글 개수를 반환하는 메서드
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post.postId = :postId AND c.isDeleted = 'live'")
    Long countByPostIdAndIsDeletedLive(Long postId);

}
