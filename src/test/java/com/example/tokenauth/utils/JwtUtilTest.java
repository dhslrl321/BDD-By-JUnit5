package com.example.tokenauth.utils;

import com.example.tokenauth.errors.InvalidTokenException;
import io.jsonwebtoken.Claims;
import org.hibernate.annotations.Source;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.shadow.com.univocity.parsers.common.ArgumentUtils;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {
    private static final Long USER_ID = 1L;
    private static final String SECRET = "12345678901234567890123456789012";
    private static final String VALID_TOKEN = "eyJhbGciOiJIUzI1NiJ9." +
            "eyJtZW1iZXJJZCI6MX0.vU91JPmJz_Kx_53C0i1p0i2NKEwTgMDOGtzMtx5UF4I";
    private static final String INVALID_TOKEN = "eyJhbGciOiJIUzI1NiJ9." +
            "eyJtZW1iZXJJZCI6MX0.vU91JPmJz_Kx_53C0i1p0i2NKEwTgMDOGtzMtx5UF40";

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET);
    }

    @Test
    @DisplayName("토큰 생성 - 정상 입력")
    void encode_with_valid_input() {
        // when
        String token = jwtUtil.encode(USER_ID);

        // then
        assertEquals(token, VALID_TOKEN);
    }

    @Test
    @DisplayName("토큰 파싱 - 정상")
    void decode_with_valid_input() {
        // when
        Claims decodedClaim = jwtUtil.decode(VALID_TOKEN);

        // then
        assertEquals(decodedClaim.get("memberId", Long.class), USER_ID);
    }

    @ParameterizedTest
    @DisplayName("토큰 파싱 - 토큰이 없는 경우")
    @NullAndEmptySource
    void decode_with_empty_input(String input) {
        // when
        InvalidTokenException invalidTokenException = assertThrows(
                InvalidTokenException.class,
                () -> jwtUtil.decode(input)
        );

        // then
        assertNotNull(invalidTokenException);
    }

    @Test
    @DisplayName("토큰 파싱 - 유효하지 않은 토큰")
    void decode_with_invalid_token() {
        // when
        InvalidTokenException invalidTokenException = assertThrows(
                InvalidTokenException.class,
                () -> jwtUtil.decode(INVALID_TOKEN)
        );
        // then
        assertNotNull(invalidTokenException);
    }
}