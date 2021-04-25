package com.example.tokenauth.controller;

import com.example.tokenauth.domain.dto.LoginRequestData;
import com.example.tokenauth.domain.entity.Role;
import com.example.tokenauth.domain.entity.RoleType;
import com.example.tokenauth.errors.LoginFailException;
import com.example.tokenauth.service.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthenticationController.class)
class AuthenticationControllerTest {

    private static final String VALID_EMAIL = "test1234@gmail.com";
    private static final String VALID_PASSWORD = "test1234";
    private static final String VALID_TOKEN = "eyJhbGciOiJIUzI1NiJ9." +
            "eyJtZW1iZXJJZCI6MX0.vU91JPmJz_Kx_53C0i1p0i2NKEwTgMDOGtzMtx5UF4I";


    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        given(authenticationService.login(VALID_EMAIL, VALID_PASSWORD)).willReturn(VALID_TOKEN);
        given(authenticationService.login("failTest@gmail.com", VALID_PASSWORD))
                .willThrow(new LoginFailException("failTest@gmail.com"));
        given(authenticationService.login(VALID_EMAIL, "failTest"))
                .willThrow(new LoginFailException(VALID_EMAIL));


    }

    @Test
    @DisplayName("로그인 - 정상 입력")
    void login_with_valid() throws Exception {
        LoginRequestData loginRequestData = LoginRequestData.builder()
                .email(VALID_EMAIL)
                .password(VALID_PASSWORD)
                .build();

        // when & then
        mockMvc.perform(post("/api/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestData)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("accessToken").exists());
    }

    @Test
    @DisplayName("로그인 - 이메일 오류")
    void login_invalid_email() throws Exception {

        LoginRequestData loginRequestData = LoginRequestData.builder()
                .email("failTest@gmail.com")
                .password(VALID_PASSWORD)
                .build();

        // when & then
        mockMvc.perform(post("/api/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestData)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("로그인 - 비밀번호 오류")
    void login_invalid_password() throws Exception {

        LoginRequestData loginRequestData = LoginRequestData.builder()
                .email(VALID_EMAIL)
                .password("failTest")
                .build();

        // when & then
        mockMvc.perform(post("/api/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequestData)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

}