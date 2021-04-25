package com.example.tokenauth.controller;

import com.example.tokenauth.domain.dto.MemberModificationData;
import com.example.tokenauth.domain.dto.MemberRequestSignUpData;
import com.example.tokenauth.domain.dto.MemberResponseData;
import com.example.tokenauth.domain.entity.Member;
import com.example.tokenauth.domain.entity.Role;
import com.example.tokenauth.domain.entity.RoleType;
import com.example.tokenauth.errors.InvalidTokenException;
import com.example.tokenauth.errors.MemberEmailDuplicationException;
import com.example.tokenauth.errors.MemberNotFoundException;
import com.example.tokenauth.service.AuthenticationService;
import com.example.tokenauth.service.MemberService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberController.class)
class MemberControllerTest {
    private static final Long DELETED_USER_ID = 100L;

    private static final String VALID_MEMBER_TOKEN = "eyJhbGciOiJIUzI1NiJ9." +
            "eyJtZW1iZXJJZCI6MX0.vU91JPmJz_Kx_53C0i1p0i2NKEwTgMDOGtzMtx5UF4I";
    private static final String VALID_ADMIN_TOKEN = "eyJhbGciOiJIUzI1NiJ9." +
            "eyJtZW1iZXJJZCI6MX0.vU91JPmJz_Kx_53C0i1p0i2NKEwTgMDOGtzMtx5UF4J";
    private static final String INVALID_TOKEN = "eyJhbGciOiJIUzI1NiJ9." +
            "eyJtZW1iZXJJZCI6MX0.vU91JPmJz_Kx_53C0i1p0i2NKEwTgMDOGtzMtx5UF40";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MemberService memberService;

    @MockBean
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        given(memberService.signUp(any(MemberRequestSignUpData.class)))
                .will(invocation -> {
                    MemberRequestSignUpData signUpData = invocation.getArgument(0);
                    return MemberResponseData.builder()
                            .email(signUpData.getEmail())
                            .nickname(signUpData.getNickname())
                            .build();
                });


        given(memberService.isExistsEmail(anyString())).willReturn(true);
        given(memberService.isExistsEmail(anyString())).willReturn(true);

        given(memberService.getMember(anyLong())).will(invocation -> {
            Long id = invocation.getArgument(0);
            if(id.equals(DELETED_USER_ID)) {
                throw new MemberNotFoundException(DELETED_USER_ID);
            }
            return MemberResponseData.builder()
                    .email("test1234@gmail.com")
                    .nickname("test1234")
                    .build();
        });

        given(memberService.getMembers(any(PageRequest.class))).will(invocation -> {
            List<MemberResponseData> members = new ArrayList<>();

            IntStream.range(1, 30).forEach(i -> {
                members.add(MemberResponseData.builder()
                        .email("test" + i + "@google.com")
                        .nickname("test" + i)
                        .build());
            });

            return new PageImpl<>(members);
        });

        given(memberService.modify(eq(1L), any(MemberModificationData.class), eq(1L))).will(invocation -> {
            MemberModificationData argument = invocation.getArgument(1);
            return MemberResponseData.builder()
                    .email("asdfasd@email.com")
                    .nickname(argument.getNickname())
                    .build();
        });

        given(memberService.modify(eq(2L), any(MemberModificationData.class), eq(1L))).willThrow(AccessDeniedException.class);

        given(authenticationService.parseToken(VALID_MEMBER_TOKEN)).willReturn(1L);
        given(authenticationService.roles(1L)).willReturn(Collections.singletonList(new Role(RoleType.USER)));

        given(authenticationService.parseToken(VALID_ADMIN_TOKEN)).willReturn(100L);
        given(authenticationService.roles(100L)).willReturn(Arrays.asList(new Role(RoleType.ADMIN), new Role(RoleType.USER)));

        given(authenticationService.parseToken(INVALID_TOKEN)).willThrow(InvalidTokenException.class);

    }

    @Test
    @DisplayName("회원가입 - 정상 입력")
    void signUp_valid() throws Exception {
        MemberRequestSignUpData data = MemberRequestSignUpData.builder()
                .email("asdf@asdf.com")
                .password("password")
                .nickname("nickname")
                .build();
        // when & then
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(data)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("email").exists());
    }

    @Test
    @DisplayName("이메일 조회 성공 - 존재하지 않는 이메일")
    void duplicateEmail_valid() throws Exception {
        // when & then
        mockMvc.perform(get("/api/members/exists/{email}", "test1234@gmail.com"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("사용자 조회 실패 - 정상 입력 - 인가되지 않은 사용자")
    void getMember_valid_with_unauthorized() throws Exception {
        // when & then
        mockMvc.perform(get("/api/members/{id}", 1)
                      .header(HttpHeaders.AUTHORIZATION, "Bearer " + INVALID_TOKEN))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("사용자 조회 성공 - 정상 입력 - 인가된 사용자")
    void getMember_valid_with_authorized() throws Exception {
        // when & then
        mockMvc.perform(get("/api/members/{id}", 1)
                       .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_MEMBER_TOKEN))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("email").exists());
    }

    @Test
    @DisplayName("사용자 조회 성공 - 정상 입력 - ADMIN 사용자")
    void getMember_valid_with_authorized_with_admin_role() throws Exception {
        // when & then
        mockMvc.perform(get("/api/members/{id}", 1)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_ADMIN_TOKEN))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("email").exists());
    }

    @Test
    @DisplayName("사용자 조회 실패 - 비정상 입력")
    void getMember_notFound() throws Exception {

        // when & then
        mockMvc.perform(get("/api/members/{id}", DELETED_USER_ID)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_MEMBER_TOKEN))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").exists());
    }

    @Test
    @DisplayName("사용자 조회 실패 - 유효하지 않은 토큰")
    void getMember_notFound_with_no_token() throws Exception {

        // when & then
        mockMvc.perform(get("/api/members/{id}", 1L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + INVALID_TOKEN))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("모든 사용자 조회 (페이징) 성공 - 관리자 조회")
    void getMembers_valid() throws Exception {

        // when & then
        mockMvc.perform(get("/api/members")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_ADMIN_TOKEN))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("content").exists());
    }

    @Test
    @DisplayName("모든 사용자 조회 (페이징) 실패 - 일반 회원 조회")
    void getMembers_invalid_member_access() throws Exception {

        // when & then
        mockMvc.perform(get("/api/members")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_MEMBER_TOKEN))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("사용자 정보 수정 성공 - 정상 입력")
    void modify_valid() throws Exception {
        // when & then
        MemberModificationData modificationData = MemberModificationData.builder()
                .nickname("newName")
                .password("newPassword")
                .build();

        mockMvc.perform(patch("/api/members/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(modificationData))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_MEMBER_TOKEN))
                .andDo(print())
                .andExpect(jsonPath("email").exists())
                .andExpect(jsonPath("nickname").exists());
    }

    @Test
    @DisplayName("사용자 정보 수정 실패 - 다른 회원 정보 수정")
    void modify_invalid_with_access_other() throws Exception {
        // when & then
        MemberModificationData modificationData = MemberModificationData.builder()
                .nickname("newName")
                .password("newPassword")
                .build();

        mockMvc.perform(patch("/api/members/{id}", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(modificationData))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_MEMBER_TOKEN))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("message").exists());
    }
}
