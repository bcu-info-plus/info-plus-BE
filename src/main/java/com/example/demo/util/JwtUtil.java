package com.example.demo.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    // Access Token의 유효기간 (10시간)
    private final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 60 * 10;

    // Refresh Token의 유효기간 (7일)
    private final long REFRESH_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24 * 7;

    // Access Token과 Refresh Token을 동시에 발급
    public Map<String, String> generateTokens(String username, Map<String, Object> additionalClaims) {
        Map<String, Object> claims = new HashMap<>(additionalClaims);

        String accessToken = createToken(claims, username, ACCESS_TOKEN_EXPIRATION);
        String refreshToken = createToken(claims, username, REFRESH_TOKEN_EXPIRATION);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        return tokens;
    }

    // 토큰을 생성하는 메서드 (유효기간을 전달받음)
    private String createToken(Map<String, Object> claims, String subject, long expiration) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    // 토큰에서 모든 Claims를 추출하는 메서드
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    // 토큰에서 username을 추출하는 메서드
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // 토큰이 만료되었는지 확인하는 메서드
    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    // Access Token을 검증하는 메서드
    public boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    // Refresh Token을 사용하여 새로운 Access Token 발급
    public String generateNewAccessToken(String refreshToken, String username) {
        if (isTokenExpired(refreshToken)) {
            throw new IllegalArgumentException("Refresh Token has expired");
        }

        // Refresh Token이 유효하면 새로운 Access Token 발급
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username, ACCESS_TOKEN_EXPIRATION);
    }
}
