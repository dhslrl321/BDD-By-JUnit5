package com.example.tokenauth.service;

import com.example.tokenauth.domain.entity.Member;
import com.example.tokenauth.domain.entity.Role;
import com.example.tokenauth.domain.repository.MemberRepository;
import com.example.tokenauth.domain.repository.RoleRepository;
import com.example.tokenauth.errors.LoginFailException;
import com.example.tokenauth.errors.MemberNotFoundException;
import com.example.tokenauth.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AuthenticationService {

    private final MemberRepository memberRepository;
    private final RoleRepository roleRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationService(MemberRepository memberRepository, RoleRepository roleRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.roleRepository = roleRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 이메일을 받아서 로그인 인증을 수행한다.
     *
     * @param email : 요청 이메일
     * @param password: 요청 비밀번호
     * @return token : Access Token
     * @throw LoginFailException : 이메일이 올바르지 않는 경우
     * @throw LoginFailException : 비밀번호가 올바르지 않는 경우
     */
    public String login(String email, String password) {

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new LoginFailException(email));

        if(!member.authenticate(password, passwordEncoder)) {
            throw new LoginFailException(email);
        }

        return jwtUtil.encode(member.getId());
    }

    /**
     * 토큰을 파싱하여 사용자 ID 를 반환한다.
     *
     * @param accessToken decode 할 토큰
     * @return 사용자 ID
     */
    public Long parseToken(String accessToken) {
        Claims claims = jwtUtil.decode(accessToken);
        return claims.get("memberId", Long.class);
    }

    /**
     * 회원의 id를 받아 해당 권한을 반환한다.
     *
     * @param memberId 권한을 조회하려는 회원 ID
     * @return 권한 리스트
     */
    public List<Role> roles(Long memberId) {
        return roleRepository.findAllByMemberId(memberId);
    }
}