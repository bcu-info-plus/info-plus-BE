package com.example.demo.service;

import com.example.demo.dto.PostRequestDTO;
import com.example.demo.model.*;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;

    public Post findById(int id) {
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

        post.setCommentsCount(0L);

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

    // 전체 게시글 조회
    public List<Post> findAll() {
        return postRepository.findAll();
    }

    // 특정 BoardType에 해당하는 게시글 조회
    public List<Post> findByBoardType(BoardType boardType) {
        return postRepository.findByBoardType(boardType);
    }

    // boardType이 있으면 필터링, 없으면 전체 게시글 조회
    public List<Post> getPostsByBoardType(String boardType) {
        if (boardType != null) {
            try {
                BoardType boardTypeEnum = BoardType.valueOf(boardType.toUpperCase());
                return findByBoardType(boardTypeEnum);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid board type");
            }
        } else {
            return findAll();
        }
    }


    public void delete(int id) {
        postRepository.deleteById(id);
    }
}
