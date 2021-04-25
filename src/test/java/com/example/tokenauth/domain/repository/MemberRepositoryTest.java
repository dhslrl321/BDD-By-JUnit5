package com.example.tokenauth.domain.repository;

import com.example.tokenauth.domain.entity.Member;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import javax.transaction.Transactional;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Transactional
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("Member 생성")
    void create() {
        // given
        String email = "dhslrl321@gmail.com";
        String password = "hello1923";

        Member member = Member.builder()
                .email(email)
                .password(password)
                .build();

        // when
        Member savedMember = memberRepository.save(member);

        // then
        assertAll(
                () -> assertEquals(email, savedMember.getEmail()),
                () -> assertNotNull(savedMember.getCreatedDate()),
                () -> assertEquals(password, savedMember.getPassword()),
                () -> assertNotNull(savedMember.getLastModifiedDate())
        );
    }

    @Test
    @DisplayName("이메일로 조회")
    @Rollback(false)
    void findMember() {
        // given
        String email = "dhslrl321@gmail";
        String password = "hwi199";
        String nickname = "dhslrl321";

        Member member = Member.builder()
                .email(email)
                .password(password)
                .nickname(nickname)
                .build();
        memberRepository.save(member);

        // when
        Optional<Member> memberOptional = memberRepository.findByEmail(email);

        // then
        memberOptional.ifPresentOrElse(
                m -> assertEquals(nickname, m.getNickname()),
                Assertions::fail
        );
    }

    @Test
    @DisplayName("중복 사용자 조회")
    void existsByEmail() {
        // given
        String email = "duplicate123@gmail.com";

        Member member = Member.builder()
                .email(email)
                .password("password123")
                .nickname("hello")
                .build();
        memberRepository.save(member);

        // when

        boolean isExists = memberRepository.existsByEmail(email);

        // then
        assertTrue(isExists);
    }
}