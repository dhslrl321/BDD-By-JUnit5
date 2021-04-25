package com.example.tokenauth.domain.dto;

import lombok.*;

import javax.validation.constraints.*;


@Getter @Setter @Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberRequestSignUpData {

    @Size(min = 3, message = "이메일은 5글자 이상이어야 합니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @NotBlank
    /*@Pattern(regexp = "^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[$@$!%*#?&])[A-Za-z[0-9]$@$!%*#?&]{8,20}$",
            message = "비밀번호는 8글자 이상 20글자 미만의 (영문, 숫자, 특수문자)의 조합으로 이루어져야 합니다.")*/
    @Size(min = 8, max = 20, message = "비밀번호는 최소 8글자 상 20글자 미만으로 이루어져야 합니다.")
    private String password;

    @NotBlank
    @Size(min = 2, max = 10, message = "닉네임은 최소 2글자 이상, 10글자 이하이어야 합니다.")
    private String nickname;
}
