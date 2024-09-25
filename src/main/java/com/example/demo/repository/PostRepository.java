package com.example.demo.repository;

import com.example.demo.model.BoardType;
import com.example.demo.model.IsDeleted;
import com.example.demo.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    // 삭제되지 않은 게시글 조회
    List<Post> findByIsDeleted(IsDeleted isDeleted);

    // 삭제되지 않은 특정 BoardType의 게시글 조회
    List<Post> findByBoardTypeAndIsDeleted(BoardType boardType, IsDeleted isDeleted);
}
