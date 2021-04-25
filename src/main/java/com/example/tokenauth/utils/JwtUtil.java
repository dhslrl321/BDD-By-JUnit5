package com.example.tokenauth.utils;

import com.example.tokenauth.errors.InvalidTokenException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
public class JwtUtil {

    private final Key key;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * 사용자 ID를 받아 토큰을 생성한다.
     *
     * @param memberId 사용자 ID 칼럼 번호
     * @return Jwt 토큰 문자열
     */
    public String encode(Long memberId) {
        return Jwts.builder()
                .claim("memberId", memberId)
                .signWith(key)
                .compact();
    }

    /**
     * 사용자 토큰을 받아 사용자 ID 를 반환한다.
     *
     * @param token Bearer 접두사가 빠진 순수 토큰 문자열 정보
     * @return 사용자 정보가 담겨있는 Claim
     * @throw InvalidTokenException 토큰이 비이었거나 null 일 때, 서버의 Secret 으로 암호화 되지 않은 토큰을 보낼 때
     */
    public Claims decode(String token) {

        if(token == null || token.isBlank() || token.isEmpty()) {
            throw new InvalidTokenException(token);
        }

        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (SignatureException e) {
            throw new InvalidTokenException(token);
        }
    }
}
