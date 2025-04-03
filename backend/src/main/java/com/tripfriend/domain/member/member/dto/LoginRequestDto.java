package com.tripfriend.domain.member.member.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class LoginRequestDto {

    @NotBlank(message = "아이디는 필수 입력값입니다")
    private String username;

    @NotBlank(message = "비밀번호는 필수 입력값입니다")
    private String password;
}
