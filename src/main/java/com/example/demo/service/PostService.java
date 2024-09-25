package com.example.demo.service;

import com.example.demo.dto.PostRequestDTO;
import com.example.demo.model.*;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.PostLikeRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;
    @Autowired
    private PostLikeRepository postLikeRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CommentRepository commentRepository;


    public Post findById(Long id) {
        return postRepository.findById(id).orElse(null);
    }


    public Post savePost(PostRequestDTO postRequest) {
        // userId를 사용해 사용자 조회
        User user = userRepository.findById(postRequest.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Post 엔티티 생성
        Post post = new Post();
        post.setTitle(postRequest.getTitle());
        post.setContent(postRequest.getContent());

        // String을 Enum으로 변환
        BoardType boardType = BoardType.valueOf(postRequest.getBoardType().toUpperCase());
        post.setBoardType(boardType); // Enum 타입으로 설정

        // LocalDateTime 현재 시간으로 설정
        post.setLocalDateTime(LocalDateTime.now());

        // IsDeleted 기본값 설정 (삭제되지 않은 상태)
        post.setIsDeleted(IsDeleted.live);

        post.setLikesCount(0L);

        post.setMajor(user.getMajor());

        // 이미지 설정 (null 체크 필요)
        if (postRequest.getImages() != null) {
            Set<Image> images = postRequest.getImages(); // postRequest에서 이미지 받아오기
            post.setImages(images);
        }

        post.setUser(user); // 조회한 사용자 설정

        // Post 저장
        return postRepository.save(post);
    }

    // 전체 게시글 조회 (삭제되지 않은 게시글만)
    public List<Post> findAll() {
        return postRepository.findByIsDeleted(IsDeleted.live);  // 삭제되지 않은 게시글 조회
    }

    // 특정 BoardType에 해당하는 게시글 조회 (삭제되지 않은 게시글만)
    public List<Post> findByBoardType(BoardType boardType) {
        return postRepository.findByBoardTypeAndIsDeleted(boardType, IsDeleted.live);  // 삭제되지 않은 게시글 조회
    }

    // boardType이 있으면 필터링, 없으면 전체 게시글 조회 (삭제되지 않은 게시글만)
    public List<Post> getPostsByBoardType(String boardType) {
        if (boardType != null) {
            try {
                BoardType boardTypeEnum = BoardType.valueOf(boardType.toUpperCase());
                return findByBoardType(boardTypeEnum);  // 특정 BoardType에 따른 게시글 조회
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid board type");
            }
        } else {
            return findAll();  // 전체 게시글 조회
        }
    }

    @Transactional
    public void deletePost(Long postId, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!post.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("You do not have permission to delete this post");
        }

        // 게시글에 연결된 댓글도 소프트 삭제 처리
        List<Comment> comments = commentRepository.findAllByPostAndIsDeleted(post, IsDeleted.live);
        for (Comment comment : comments) {
            comment.setIsDeleted(IsDeleted.deleted);
            commentRepository.save(comment);
        }

        // 게시글 소프트 삭제
        post.setIsDeleted(IsDeleted.deleted);
        postRepository.save(post);
    }

    // 게시글 수정
    public Post updatePost(Long postId, PostRequestDTO postRequest, User user) {
        // 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // 게시글 작성자와 현재 사용자가 동일한지 확인
        if (!post.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("You do not have permission to edit this post");
        }

        // 게시글 내용 수정
        post.setTitle(postRequest.getTitle());
        post.setContent(postRequest.getContent());

        // BoardType 수정
        BoardType boardType = BoardType.valueOf(postRequest.getBoardType().toUpperCase());
        post.setBoardType(boardType);

        // 이미지 수정
        if (postRequest.getImages() != null) {
            Set<Image> images = postRequest.getImages();
            post.setImages(images);
        }

        return postRepository.save(post);
    }

}
