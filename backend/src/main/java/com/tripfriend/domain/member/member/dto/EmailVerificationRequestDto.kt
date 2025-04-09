package com.tripfriend.domain.member.member.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class EmailVerificationRequestDto(
    @field:Email(message = "올바른 이메일 형식을 입력해주세요.")
    @field:NotBlank(message = "이메일은 필수 입력값입니다.")
    val email: String = "",

    @field:NotBlank(message = "인증 코드는 필수 입력값입니다.")
    @field:Size(min = 6, max = 6, message = "인증 코드는 6자리여야 합니다.")
    val authCode: String = ""
)
