package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public AuthenticationService(AuthenticationManager authenticationManager, UserRepository userRepository, JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    public Map<String, String> login(String email, String password) {
        try {
            // 이메일로 사용자 정보 인증 시도
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            // UserDetailsService를 통해 사용자 로드
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // 이메일로 사용자 정보 로드
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + userDetails.getUsername()));

            // JWT 토큰 생성
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", user.getUserId());
            claims.put("email", user.getEmail());
            claims.put("name", user.getName());
            claims.put("major", user.getMajor());

            // Access Token과 Refresh Token 생성
            return jwtUtil.generateTokens(user.getEmail(), claims);

        } catch (UsernameNotFoundException e) {
            throw new UsernameNotFoundException("Invalid email or password");
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid email or password");
        } catch (Exception e) {
            throw new RuntimeException("An unexpected error occurred during login");
        }
    }

    public String refreshAccessToken(String refreshToken) {
        try {
            // Refresh Token에서 사용자 이메일을 추출
            String username = jwtUtil.extractUsername(refreshToken);

            // Refresh Token이 유효한 경우 새로운 Access Token 발급
            return jwtUtil.generateNewAccessToken(refreshToken, username);

        } catch (Exception e) {
            throw new RuntimeException("Invalid or expired refresh token");
        }
    }
}
