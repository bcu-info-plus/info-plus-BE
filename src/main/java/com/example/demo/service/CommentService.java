package com.example.demo.service;

import com.example.demo.dto.CommentDTO;
import com.example.demo.model.Comment;
import com.example.demo.model.IsDeleted;
import com.example.demo.model.Post;
import com.example.demo.model.User;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CommentDTO createComment(Long postId, Long userId, String content, Long parentId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setPost(post);
        comment.setUser(user);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setIsDeleted(IsDeleted.live);

        // parentId가 null이 아닌 경우, 부모 댓글을 설정
        if (parentId != null) {
            Comment parentComment = commentRepository.findById(parentId)
                    .orElseThrow(() -> new IllegalArgumentException("Parent comment not found"));
            comment.setParent(parentComment);
        }

        Comment savedComment = commentRepository.save(comment);

        // Comment 엔티티를 DTO로 변환하여 반환
        return new CommentDTO(
                savedComment.getCommentId(),
                savedComment.getContent(),
                savedComment.getUser().getName(),
                savedComment.getUser().getMajor(),
                savedComment.getCreatedAt(),
                savedComment.getParent() != null ? savedComment.getParent().getCommentId() : null // 부모 ID 추가
        );
    }


    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        if (!comment.getUser().getUserId().equals(userId)) {
            throw new IllegalStateException("User not authorized to delete this comment");
        }

        comment.setIsDeleted(IsDeleted.deleted);
        commentRepository.save(comment);
    }

    public Optional<Comment> getComment(Long commentId) {
        return commentRepository.findById(commentId);
    }

    public List<CommentDTO> getCommentsByPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        List<Comment> comments = commentRepository.findAllByPostAndIsDeleted(post, IsDeleted.live);

        return comments.stream()
                .map(comment -> new CommentDTO(
                        comment.getCommentId(),
                        comment.getContent(),
                        comment.getUser().getName(),     // 작성자 이름
                        comment.getUser().getMajor(),    // 작성자 학과
                        comment.getCreatedAt(),
                        comment.getParent() != null ? comment.getParent().getCommentId() : null  // parentId 추가
                ))
                .collect(Collectors.toList());
    }

    // 특정 게시글의 댓글 개수를 반환하는 메서드
    public Long countCommentsByPostId(Long postId) {
        return commentRepository.countByPostIdAndIsDeletedLive(postId);
    }

}
