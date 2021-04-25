package com.example.tokenauth.domain.entity;

import com.example.tokenauth.domain.dto.MemberModificationData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class MemberTest {

    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
    }

    @Test
    @DisplayName("비밀번호 암호화")
    void changePassword() {
        // given
        Member member = Member.builder().build();

        // when
        member.changePassword("TEST", passwordEncoder);

        // then
        assertNotEquals(member.getPassword(), "TEST");
        assertNotNull(member.getPassword());
    }

    @Test
    @DisplayName("닉네임 변경")
    void change_nickname() {
        // given
        String nickname = "nickname";
        String newNickname = "new nickname";

        Member member = Member.builder()
                .nickname(nickname)
                .password("password")
                .build();

        MemberModificationData memberModificationData = MemberModificationData.builder()
                .nickname(newNickname)
                .build();
        // when
        member.changeNicknameFrom(memberModificationData);

        // then
        assertNotEquals(member.getNickname(), nickname);
        assertEquals(member.getNickname(), newNickname);
    }

    @Test
    @DisplayName("비밀번호 검증")
    void authenticate() {
        // given
        String password = "password";

        Member member = Member.builder().build();

        member.changePassword(password, passwordEncoder);
        // when
        boolean isAuthenticated = member.authenticate(password, passwordEncoder);

        // then
        assertTrue(isAuthenticated);
    }
}