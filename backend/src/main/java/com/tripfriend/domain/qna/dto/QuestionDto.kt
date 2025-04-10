package com.tripfriend.domain.qna.dto

import com.tripfriend.domain.qna.entity.Question

data class QuestionDto(
    val id: Long?,
    val title: String,
    val content: String,
    val createdAt: String?,
    val updatedAt: String?,
    val memberUsername: String?
) {
    constructor(question: Question) : this(
        id = question.id,
        title = question.title,
        content = question.content,
        createdAt = question.createdAt.toString(),
        updatedAt = question.updatedAt.toString(),
        memberUsername = question.member.username
    )
}