package com.tripfriend.domain.review.dto

import com.tripfriend.domain.review.entity.Comment
import java.time.LocalDateTime

data class CommentResponseDto(
    val commentId: Long? = null,
    val content: String = "",
    val reviewId: Long? = null,
    val memberId: Long? = null,
    val memberName: String = "",
    var profileImage: String? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    constructor(comment: Comment, memberName: String) : this(
        commentId = comment.commentId,
        content = comment.content,
        reviewId = comment.review?.reviewId,
        memberId = comment.member?.id,
        memberName = memberName,
        profileImage = null,
        createdAt = comment.createdAt,
        updatedAt = comment.updatedAt
    )

    constructor(comment: Comment, memberName: String, profileImage: String?) : this(
        commentId = comment.commentId,
        content = comment.content,
        reviewId = comment.review?.reviewId,
        memberId = comment.member?.id,
        memberName = memberName,
        profileImage = profileImage,
        createdAt = comment.createdAt,
        updatedAt = comment.updatedAt
    )
}