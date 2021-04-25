package com.example.tokenauth.domain.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberModificationData {

    @Size(min = 8, max = 20, message = "비밀번호는 최소 8글자 상 20글자 미만으로 이루어져야 합니다.")
    private String password;

    @Size(min = 2, max = 10, message = "닉네임은 최소 2글자 이상, 10글자 이하이어야 합니다.")
    private String nickname;
}
