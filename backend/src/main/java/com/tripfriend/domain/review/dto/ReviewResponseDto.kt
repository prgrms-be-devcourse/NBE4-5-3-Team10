package com.tripfriend.domain.review.dto

import com.tripfriend.domain.review.entity.Review
import java.time.LocalDateTime

data class ReviewResponseDto(
    val reviewId: Long? = null,
    val title: String = "",
    val content: String = "",
    val rating: Double = 0.0,
    val memberId: Long? = null,
    val memberName: String = "",
    var profileImage: String? = null, // var로 변경하여 수정 가능하게 함
    val placeId: Long? = null,
    val placeName: String? = null,
    var viewCount: Int = 0,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
    val commentCount: Int = 0
) {
    constructor(review: Review, memberName: String, commentCount: Int) : this(
        reviewId = review.reviewId,
        title = review.title,
        content = review.content,
        rating = review.rating,
        memberId = review.member?.id,
        memberName = memberName,
        profileImage = null,
        placeId = review.place?.id,
        placeName = review.place?.placeName,
        viewCount = 0,
        createdAt = review.createdAt,
        updatedAt = review.updatedAt,
        commentCount = commentCount
    )

    constructor(review: Review, memberName: String, commentCount: Int, profileImage: String?) : this(
        reviewId = review.reviewId,
        title = review.title,
        content = review.content,
        rating = review.rating,
        memberId = review.member?.id,
        memberName = memberName,
        profileImage = profileImage,
        placeId = review.place?.id,
        placeName = review.place?.placeName,
        viewCount = 0,
        createdAt = review.createdAt,
        updatedAt = review.updatedAt,
        commentCount = commentCount
    )
}