package com.example.tokenauth.domain.dto;

import lombok.*;
import org.springframework.validation.Errors;

@Getter @Setter @Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberResponseData {
    private String email;
    private String nickname;
}
