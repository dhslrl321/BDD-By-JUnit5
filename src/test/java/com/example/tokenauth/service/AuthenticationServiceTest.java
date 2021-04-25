package com.example.tokenauth.service;

import com.example.tokenauth.domain.entity.Member;
import com.example.tokenauth.domain.entity.Role;
import com.example.tokenauth.domain.entity.RoleType;
import com.example.tokenauth.domain.repository.MemberRepository;
import com.example.tokenauth.domain.repository.RoleRepository;
import com.example.tokenauth.errors.InvalidTokenException;
import com.example.tokenauth.errors.LoginFailException;
import com.example.tokenauth.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class AuthenticationServiceTest {

    private static final String SECRET = "12345678901234567890123456789012";

    private static final String VALID_EMAIL = "testUser123@gmail.com";
    private static final String VALID_PASSWORD = "password123";

    private static final String VALID_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJtZW1iZXJJZCI6MX0." +
            "vU91JPmJz_Kx_53C0i1p0i2NKEwTgMDOGtzMtx5UF4I";
    private static final String INVALID_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJtZW1iZXJJZCI6MX0." +
            "vU91JPmJz_Kx_53C0i1p0i2NKEwTgMDOGtzMtx5UF2I";

    private static final Long MEMBER_ID = 1L;
    private static final Long ADMIN_ID = 100L;


    private AuthenticationService authenticationService;

    private final MemberRepository memberRepository = mock(MemberRepository.class);
    private final RoleRepository roleRepository = mock(RoleRepository.class);

    @BeforeEach
    void setUp() {
        JwtUtil jwtUtil = new JwtUtil(SECRET);
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        authenticationService = new AuthenticationService(memberRepository,
                                                            roleRepository,
                jwtUtil,
                passwordEncoder);

        Member member = Member.builder()
                .id(1L)
                .email(VALID_EMAIL)
                .password(VALID_PASSWORD)
                .nickname("Test Member")
                .build();

        member.changePassword(member.getPassword(), new BCryptPasswordEncoder());

        given(memberRepository.findByEmail("testUser123@gmail.com")).willReturn(Optional.of(member));
        given(roleRepository.findAllByMemberId(MEMBER_ID))
                .willReturn(Collections.singletonList(new Role(RoleType.USER)));
        given(roleRepository.findAllByMemberId(ADMIN_ID))
                .willReturn(Arrays.asList(new Role(RoleType.ADMIN), new Role(RoleType.USER)));
    }

    @Test
    @DisplayName("로그인 - 정상 입력")
    void login_with_valid() {
        // when
        String accessToken = authenticationService.login(VALID_EMAIL, VALID_PASSWORD);

        // then
        assertEquals(accessToken, VALID_TOKEN);
    }

    @Test
    @DisplayName("로그인 - 실패: 존재하지 않는 이메일")
    void login_fail_invalid_email() {
        // when
        LoginFailException loginFailException = assertThrows(LoginFailException.class,
                () -> authenticationService.login("asdfasd@asdfa.com", VALID_PASSWORD)
        );

        // then
        assertNotNull(loginFailException.getMessage());
    }

    @Test
    @DisplayName("로그인 - 실패: 일치하지 않는 비밀번호")
    void login_fail_invalid_password() {
        // when
        LoginFailException loginFailException = assertThrows(LoginFailException.class,
                () -> authenticationService.login(VALID_EMAIL, "hello12")
        );
        // then
        assertNotNull(loginFailException.getMessage());
    }

    @Test
    @DisplayName("토큰 복호 - 성공")
    void parseToken_valid() {
        // when
        Long memberId = authenticationService.parseToken(VALID_TOKEN);

        // then
        assertEquals(memberId, 1L);
    }

    @Test
    @DisplayName("토큰 복호 - 실패")
    void parseToken_invalid() {
        // when
        InvalidTokenException invalidTokenException = assertThrows(
                InvalidTokenException.class,
                () -> authenticationService.parseToken(INVALID_TOKEN)
        );

        // then
        assertNotNull(invalidTokenException);
    }

    @Test
    @DisplayName("권한 조회 - 사용자")
    void roles_member() {
        // when
        List<Role> roles = authenticationService.roles(MEMBER_ID);

        // then
        assertEquals(roles.get(0).getRoleType(), RoleType.USER);
    }

    @Test
    @DisplayName("권한 조회 - 관리자")
    void roles_admin() {
        // when
        List<Role> roles = authenticationService.roles(ADMIN_ID);

        // then
        assertEquals(roles.get(0).getRoleType(), RoleType.ADMIN);
        assertEquals(roles.get(1).getRoleType(), RoleType.USER);
    }

}