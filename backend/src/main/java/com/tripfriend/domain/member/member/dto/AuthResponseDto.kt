package com.tripfriend.domain.member.member.dto

data class AuthResponseDto(
    val accessToken: String,
    val refreshToken: String? = null,
    val isDeletedAccount: Boolean = false,
    val authority: String? = null
) {

    constructor(accessToken: String, refreshToken: String?, isDeletedAccount: Boolean) : this(
        accessToken = accessToken,
        refreshToken = refreshToken,
        isDeletedAccount = isDeletedAccount,
        authority = null  // 권한 정보 없이 호출될 경우 null로 설정
    )
}