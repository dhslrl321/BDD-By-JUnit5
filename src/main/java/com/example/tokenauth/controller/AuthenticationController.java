package com.example.tokenauth.controller;

import com.example.tokenauth.domain.dto.LoginRequestData;
import com.example.tokenauth.domain.dto.LoginResponseData;
import com.example.tokenauth.service.AuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/authenticate", produces = "application/json; charset=utf-8")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping
    public ResponseEntity<LoginResponseData> login(@RequestBody LoginRequestData loginRequestData) {

        String email = loginRequestData.getEmail();
        String password = loginRequestData.getPassword();

        String accessToken = authenticationService.login(email, password);

        return ResponseEntity.status(HttpStatus.OK).body(LoginResponseData.builder()
                .accessToken(accessToken)
                .build());
    }
}
