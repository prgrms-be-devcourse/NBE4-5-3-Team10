package com.tripfriend.domain.qna.dto

import com.tripfriend.domain.qna.entity.Answer

data class AnswerDto(
    val answerId: Long?,
    val content: String,
    val memberUsername: String,
    val createdAt: String
) {
    constructor(answer: Answer) : this(
        answerId = answer.id,
        content = answer.content,
        memberUsername = answer.member.username,
        createdAt = answer.createdAt.toString()
    )
}