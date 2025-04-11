package com.tripfriend.domain.member.member.dto

import jakarta.validation.constraints.NotBlank

data class LoginRequestDto(
    @field:NotBlank(message = "아이디는 필수 입력값입니다")
    val username: String = "",

    @field:NotBlank(message = "비밀번호는 필수 입력값입니다")
    val password: String = ""
)
