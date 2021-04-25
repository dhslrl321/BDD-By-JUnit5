package com.example.tokenauth.domain.repository;

import com.example.tokenauth.domain.entity.Member;
import com.example.tokenauth.domain.entity.Role;
import com.example.tokenauth.domain.entity.RoleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.transaction.Transactional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Transactional
class RoleRepositoryTest {
    
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("회원 저장시 Role 저장")
    void create() {
        // given
        Member member = Member.builder()
                .email("testForRole@naver.com")
                .password("testRole1234")
                .nickname("RoleTestUser")
                .build();
        member.changePassword(member.getPassword(), new BCryptPasswordEncoder());

        Member savedMember = memberRepository.save(member);
        Role role = new Role(savedMember.getId(), RoleType.USER);

        // when
        Role savedRole = roleRepository.save(role);

        // then
        assertEquals(savedRole.getRoleType(), RoleType.USER);
    }

}