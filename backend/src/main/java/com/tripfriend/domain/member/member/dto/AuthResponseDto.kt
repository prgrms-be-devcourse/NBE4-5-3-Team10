package com.tripfriend.domain.member.member.dto

data class AuthResponseDto(
    var accessToken: String? = null,
    var refreshToken: String? = null,
    var isDeletedAccount: Boolean = false
) {
    constructor(accessToken: String) : this(
        accessToken = accessToken,
        refreshToken = null,
        isDeletedAccount = false
    )

    constructor(accessToken: String, isDeletedAccount: Boolean) : this(
        accessToken = accessToken,
        refreshToken = null,
        isDeletedAccount = isDeletedAccount
    )
}