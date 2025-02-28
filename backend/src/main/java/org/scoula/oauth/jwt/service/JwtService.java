// JwtUtil.java
package org.scoula.oauth.jwt.service;

import io.jsonwebtoken.*;
import org.scoula.oauth.domain.vo.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@PropertySource("classpath:application.properties")
/*
 * JWT 토큰을 생성하고 검증하는 유틸리티 클래스
 */
public class JwtService {

    private final String secretKey; // 시크릿 키
    private final long expirationTime; // 만료 시간 (밀리초)

    public JwtService(@Value("${jwt.secret}") String secretKey, @Value("${jwt.expired}") long expirationTime) {
        this.secretKey = secretKey;
        this.expirationTime = expirationTime;
    }

    /**
     * JWT 토큰을 생성하는 메서드
     */
    public String generateToken(String userId, Role role) {
        long now = System.currentTimeMillis();
        Date expiryDate = new Date(now + expirationTime);

        // JWT 생성
        return Jwts.builder()
                .setSubject(userId)
                .claim("role", role.name())
                .setIssuedAt(new Date(now))
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, secretKey.getBytes())
                .compact();
    }

    /**
     * JWT 토큰을 검증하고 유효성을 확인하는 메서드
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey.getBytes()).parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) { // 토큰 만료 예외
            System.err.println("Expired JWT token: " + e.getMessage());
        } catch (SignatureException e) { // 시그니처 불일치
            System.err.println("Invalid JWT signature: " + e.getMessage());
        } catch (MalformedJwtException e) { // 잘못된 토큰
            System.err.println("Invalid JWT token: " + e.getMessage());
        } catch (Exception e) { // 기타 예외
            System.err.println("JWT validation error: " + e.getMessage());
        }
        return false; // 토큰이 유효하지 않음
    }

    /**
     * JWT 토큰에서 사용자 ID를 추출하는 메서드
     */
    public String getUserIdFromToken(String token) {
        Claims claims = Jwts.parser().setSigningKey(secretKey.getBytes()).parseClaimsJws(token).getBody();
        return claims.getSubject();
    }

    /**
     * JWT 토큰에서 만료 시간을 추출하는 메서드
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = Jwts.parser().setSigningKey(secretKey.getBytes()).parseClaimsJws(token).getBody();
        return claims.getExpiration();
    }
}
