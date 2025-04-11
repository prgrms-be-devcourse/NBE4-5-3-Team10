package com.tripfriend.domain.member.member.dto

data class TokenInfoDto(
    val username: String,
    val authority: String,
    val verified: Boolean
)
