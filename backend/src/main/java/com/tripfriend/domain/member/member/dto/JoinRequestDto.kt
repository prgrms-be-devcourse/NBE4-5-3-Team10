package com.tripfriend.domain.member.member.dto

import com.tripfriend.domain.member.member.entity.AgeRange
import com.tripfriend.domain.member.member.entity.Gender
import com.tripfriend.domain.member.member.entity.Member
import com.tripfriend.domain.member.member.entity.TravelStyle
import jakarta.validation.constraints.*

data class JoinRequestDto(
    @field:NotBlank(message = "아이디는 필수 입력값입니다.")
    @field:Size(min = 4, max = 20, message = "아이디는 4자 이상 20자 이하여야 합니다.")
    @field:Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "아이디는 영문자, 숫자, 언더스코어만 사용 가능합니다.")
    var username: String = "",

    @field:NotBlank(message = "이메일은 필수 입력값입니다.")
    @field:Email(message = "이메일 형식이 올바르지 않습니다.")
    var email: String = "",

    @field:NotBlank(message = "비밀번호는 필수 입력값입니다.")
    @field:Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    var password: String = "",

    @field:NotBlank(message = "닉네임은 필수 입력값입니다")
    var nickname: String = "",

    var profileImage: String? = null,

    @field:NotNull(message = "성별은 필수 입력값입니다.(남성 / 여성)")
    var gender: Gender? = null,

    @field:NotNull(message = "나이대는 필수 입력값입니다.(10대 / 20대 / 30대 / 40대 이상)")
    var ageRange: AgeRange? = null,

    @field:NotNull(message = "여행 스타일은 필수 입력값입니다.(관광 / 휴양 / 쇼핑)")
    var travelStyle: TravelStyle? = null,

    var aboutMe: String? = null
) {
    fun toEntity(): Member {
        return Member(
            username = username,
            email = email,
            password = password,
            nickname = nickname,
            profileImage = profileImage ?: "",
            gender = gender ?: throw IllegalStateException("성별은 필수 입력값입니다."),
            ageRange = ageRange ?: throw IllegalStateException("나이대는 필수 입력값입니다."),
            travelStyle = travelStyle ?: throw IllegalStateException("여행 스타일은 필수 입력값입니다."),
            aboutMe = aboutMe ?: "",
            rating = 0.0,
            authority = "USER",
            verified = false
        )
    }
}
