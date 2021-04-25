package com.example.tokenauth.service;

import com.example.tokenauth.domain.dto.MemberModificationData;
import com.example.tokenauth.domain.dto.MemberRequestSignUpData;
import com.example.tokenauth.domain.dto.MemberResponseData;
import com.example.tokenauth.domain.entity.Member;
import com.example.tokenauth.domain.repository.MemberRepository;
import com.example.tokenauth.domain.repository.RoleRepository;
import com.example.tokenauth.errors.MemberEmailDuplicationException;
import com.example.tokenauth.errors.MemberNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

class MemberServiceTest {
    private static final Long DELETED_USER_ID = 100L;
    private static final int PAGE_INDEX = 0;
    private static final int PAGE_SIZE = 5;
    private static final String EXIST_EMAIL = "exists1234@gmail.com";
    private static final String PASSWORD = "test1234";
    private static final String NICKNAME = "James";
    private MemberService memberService;

    private final MemberRepository memberRepository = mock(MemberRepository.class);
    private final RoleRepository roleRepository = mock(RoleRepository.class);

    @BeforeEach
    void setUp() {
        ModelMapper modelMapper = new ModelMapper();
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        memberService = new MemberService(memberRepository, roleRepository, passwordEncoder, modelMapper);

        Member member = Member.builder()
                .email(EXIST_EMAIL)
                .password(PASSWORD)
                .nickname(NICKNAME)
                .build();

        List<Member> members = new ArrayList<>();

        IntStream.range(PAGE_INDEX, PAGE_SIZE).forEach(each -> {
            Member memberEach = Member.builder()
                    .email("test" + each + "@test.test")
                    .password("test1234")
                    .nickname("Test User " + each)
                    .build();
            members.add(memberEach);
        });
        Page<Member> pagedMembers = new PageImpl<>(members);
        given(memberRepository.findAll(PageRequest.of(PAGE_INDEX, PAGE_SIZE))).willReturn(pagedMembers);
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(memberRepository.findById(DELETED_USER_ID)).willReturn(Optional.empty());
        given(memberRepository.save(any(Member.class))).will(invocation -> {
            Member source = invocation.getArgument(0);
            source.changePassword(source.getPassword(), passwordEncoder);
            return Member.builder()
                    .email(source.getEmail())
                    .nickname(source.getNickname())
                    .password(source.getPassword())
                    .build();
        });

        given(memberRepository.existsByEmail(any(String.class))).willReturn(false);

        given(memberRepository.existsByEmail(EXIST_EMAIL)).willReturn(true);
    }

    @Test
    @DisplayName("회원가입 테스트")
    void signUp_valid() {
        // when
        MemberResponseData savedMember = memberService.signUp(MemberRequestSignUpData.builder()
                                                            .email("test123@naver.com")
                                                            .password(PASSWORD)
                                                            .nickname(NICKNAME)
                                                            .build());
        // then
        assertEquals(NICKNAME, savedMember.getNickname());
    }

    @Test
    @DisplayName("회원가입 테스트 - 존재하는 사용자")
    void signUp_invalid_with_exists_email() {
        // when
        MemberRequestSignUpData member = MemberRequestSignUpData.builder()
                .email(EXIST_EMAIL)
                .password(PASSWORD)
                .nickname(NICKNAME)
                .build();

        MemberEmailDuplicationException duplicationException = assertThrows(MemberEmailDuplicationException.class,
                        () -> memberService.signUp(member)
        );
        // then
        assertEquals(EXIST_EMAIL + " 는 이미 존재하는 Email 입니다.", duplicationException.getMessage());
    }

    @Test
    @DisplayName("이메일 존재여부 확인 - 존재하지 않는 사용자")
    void email_exists_valid() {
        // when
        boolean isExist = memberService.isExistsEmail("test1234@gmail.com");
        // then
        assertFalse(isExist);
    }

    @Test
    @DisplayName("이메일 존재여부 확인 - 존재하는 사용자")
    void email_exists_invalid() {
        MemberEmailDuplicationException duplicationException = assertThrows(MemberEmailDuplicationException.class,
                () -> memberService.isExistsEmail(EXIST_EMAIL)
        );

        assertNotNull(duplicationException);
    }

    @Test
    @DisplayName("사용자 단건 조회")
    void getMember() {
        // when
        MemberResponseData selectedMember = memberService.getMember(1L);

        // then
        assertEquals(NICKNAME, selectedMember.getNickname());
    }

    @Test
    @DisplayName("사용자 단건 조회 - 없는 사용자 조회")
    void getMember_not_found() {
        // when
        MemberNotFoundException memberNotFoundException = assertThrows(MemberNotFoundException.class,
                () -> memberService.getMember(DELETED_USER_ID)
        );
        // then
        assertEquals("member not found: " + DELETED_USER_ID, memberNotFoundException.getMessage());
    }

    @Test
    @DisplayName("모든 사용자 조회")
    void getMembers() {
        // when
        PageRequest pageRequest = PageRequest.of(PAGE_INDEX, PAGE_SIZE);

        // then
        Page<MemberResponseData> pagedData = memberService.getMembers(pageRequest);
        assertEquals(PAGE_SIZE, pagedData.getTotalElements());
        assertEquals(1, pagedData.getTotalPages());
    }

    @Test
    @DisplayName("사용자 정보 수정 성공 - 정상 입력")
    void update_valid() {
        Long targetId = 1L;
        Long parsedId = 1L;

        MemberModificationData memberModificationData = MemberModificationData.builder()
                .nickname("new_nickname")
                .password("new_password")
                .build();

        // when
        MemberResponseData modifiedData = memberService.modify(targetId, memberModificationData, parsedId);

        // then
        assertEquals(modifiedData.getNickname(), "new_nickname");
    }

}