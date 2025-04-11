package com.tripfriend.domain.member.member.dto

import com.tripfriend.domain.member.member.entity.AgeRange
import com.tripfriend.domain.member.member.entity.Gender
import com.tripfriend.domain.member.member.entity.TravelStyle

data class MemberUpdateRequestDto(
    var nickname: String? = null,
    var email: String? = null,
    var password: String? = null,
    var profileImage: String? = null,
    var gender: Gender? = null,
    var ageRange: AgeRange? = null,
    var travelStyle: TravelStyle? = null,
    var aboutMe: String? = null
)
