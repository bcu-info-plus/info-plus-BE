package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil; // JWT 유틸리티 클래스

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(@RequestHeader("Authorization") String token) {
        try {
            // Bearer 토큰에서 실제 JWT 토큰 추출
            if (token == null || !token.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token format");
            }

            String jwtToken = token.substring(7); // "Bearer " 이후의 JWT 토큰 부분만 추출

            // JWT 토큰에서 이메일 추출 (예외 처리 추가)
            String email;
            try {
                email = jwtUtil.extractUsername(jwtToken);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token");
            }

            // 이메일로 사용자 정보 로드
            User user;
            try {
                user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
            } catch (UsernameNotFoundException ex) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found with email: " + email);
            }

            // 사용자 정보를 반환 (nickname, name, email, profileImage 가정)
            Map<String, Object> userProfile = new HashMap<>();
            userProfile.put("userId", user.getUserId());
            userProfile.put("nickname", user.getName()); // nickname을 name으로 가정
            userProfile.put("name", user.getName());
            userProfile.put("email", user.getEmail());
            userProfile.put("profileImage", "default.jpg"); // 프로필 이미지 가정

            return ResponseEntity.ok(userProfile);

        } catch (Exception e) {
            // 로깅 추가하여 에러를 확인
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }

}
