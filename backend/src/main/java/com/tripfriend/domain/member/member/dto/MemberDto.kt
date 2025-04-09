package com.tripfriend.domain.member.member.dto

import com.tripfriend.domain.member.member.entity.Member

data class MemberDto(
    val id: Long?,
    val username: String
) {
    constructor(member: Member) : this(
        id = member.id,
        username = member.username
    )
}
