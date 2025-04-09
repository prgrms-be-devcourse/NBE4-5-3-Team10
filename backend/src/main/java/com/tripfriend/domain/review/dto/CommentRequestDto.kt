package com.tripfriend.domain.review.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CommentRequestDto(
    @field:NotBlank(message = "댓글 내용은 필수 입력 항목입니다.")
    @field:Size(min = 2, max = 100, message = "댓글은 2자 이상 100자 이하로 입력해주세요.")
    var content: String = "",

    var reviewId: Long? = null
)