package com.example.demo.service;

import com.example.demo.dto.PostRequestDTO;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

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
    @Autowired
    private ImageRepository imageRepository;


    public Post findById(Long id) {
        return postRepository.findById(id).orElse(null);
    }

    // application.properties 파일에서 파일 저장 경로를 읽어옴
    @Value("${file.upload-dir}")
    private String uploadDir;

    // 이미지를 서버에 저장하고 Image 엔티티로 변환하는 메서드
    private Set<Image> saveImages(Set<MultipartFile> imageFiles, Post post) {
        Set<Image> images = new HashSet<>();

        // null 또는 빈 이미지 파일 체크
        if (imageFiles != null && !imageFiles.isEmpty()) {
            for (MultipartFile file : imageFiles) {
                String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                // 서버 외부 경로로 설정 (절대 경로)
                String filePath = "/home/webserver/demo/uploads/" + fileName;

                try {
                    // 실제 파일을 저장
                    File imageFile = new File(filePath);
                    file.transferTo(imageFile);

                    // Image 엔티티 생성
                    Image image = new Image();
                    // DB에 저장되는 경로는 절대 경로가 아닌 URL 경로로 설정
                    image.setImageUrl("/uploads/" + fileName);  // DB에 저장될 경로 (URL)

                    image.setPost(post);  // 이미지와 해당 게시물을 연결
                    image.setBoardType(post.getBoardType());  // 게시물의 BoardType 설정

                    // Image 엔티티 저장 및 추가
                    imageRepository.save(image);
                    images.add(image);

                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException("Failed to store image file", e);
                }
            }
        }
        return images;
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

        // 기본값 설정
        post.setIsDeleted(IsDeleted.live); // 삭제되지 않은 상태
        post.setLikesCount(0L);
        post.setMajor(user.getMajor());
        post.setUser(user); // 조회한 사용자 설정

        // **Post를 먼저 저장**
        post = postRepository.save(post); // 이 시점에서 post는 영속성 컨텍스트에 저장됨.

        // 이미지 저장
        if (postRequest.getImages() != null && !postRequest.getImages().isEmpty()) {
            Set<Image> imageEntities = saveImages(postRequest.getImages(), post); // post가 저장된 후에 이미지를 저장
            post.setImages(imageEntities); // Post와 새 이미지 연결
        }

        // 최종적으로 다시 Post 반환
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


    public Post updatePost(Long postId, PostRequestDTO postRequest, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // 기존 Post의 타이틀, 내용, 기타 속성 업데이트
        post.setTitle(postRequest.getTitle());
        post.setContent(postRequest.getContent());
        post.setBoardType(BoardType.valueOf(postRequest.getBoardType().toUpperCase()));

        // **기존 이미지 처리**: 기존 이미지 ID로 기존 이미지를 유지
        Set<Image> existingImages = post.getImages(); // 기존 이미지들
        if (postRequest.getExistingImages() != null && !postRequest.getExistingImages().isEmpty()) {
            // 기존 이미지 ID로 이미지 가져오기
            List<Image> imagesFromDb = imageRepository.findAllById(postRequest.getExistingImages());
            existingImages.clear(); // 기존 이미지 세트를 클리어 (덮어쓰기 방지)
            existingImages.addAll(imagesFromDb); // 기존 이미지들 유지
        } else {
            existingImages.clear(); // 기존 이미지를 전부 제거하는 상황 처리
        }

        // **새 이미지 처리**: 새로 추가된 이미지를 저장
        if (postRequest.getImages() != null && !postRequest.getImages().isEmpty()) {
            Set<Image> newImages = saveImages(postRequest.getImages(), post); // 새 이미지 저장
            existingImages.addAll(newImages); // 기존 이미지와 새 이미지를 병합
        }

        post.setImages(existingImages); // 최종적으로 이미지 목록을 Post에 설정

        return postRepository.save(post); // 최종 저장
    }



}
