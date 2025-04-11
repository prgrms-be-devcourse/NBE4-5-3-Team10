package com.tripfriend.domain.member.member.dto

import com.tripfriend.domain.member.member.entity.AgeRange
import com.tripfriend.domain.member.member.entity.Gender
import com.tripfriend.domain.member.member.entity.Member
import com.tripfriend.domain.member.member.entity.TravelStyle
import java.time.LocalDateTime

data class MemberResponseDto(
    val id: Long?,
    val username: String,
    val email: String,
    val nickname: String,
    val profileImage: String?,
    val gender: Gender,
    val ageRange: AgeRange,
    val travelStyle: TravelStyle,
    val aboutMe: String?,
    val rating: Double,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val authority: String
) {
    companion object {
        @JvmStatic
        fun fromEntity(member: Member): MemberResponseDto {
            return MemberResponseDto(
                id = member.id,
                username = member.username,
                email = member.email,
                nickname = member.nickname,
                profileImage = member.profileImage,
                gender = member.gender,
                ageRange = member.ageRange,
                travelStyle = member.travelStyle,
                aboutMe = member.aboutMe,
                rating = member.rating,
                createdAt = member.createdAt,
                updatedAt = member.updatedAt,
                authority = member.authority
            )
        }
    }
}
