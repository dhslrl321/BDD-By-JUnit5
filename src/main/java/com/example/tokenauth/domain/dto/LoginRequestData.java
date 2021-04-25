package com.example.tokenauth.domain.dto;

import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequestData {
    private String email;
    private String password;
}
