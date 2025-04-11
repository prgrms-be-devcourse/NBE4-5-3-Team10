package com.tripfriend.domain.review.repository

import com.tripfriend.domain.review.entity.Comment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CommentRepository : JpaRepository<Comment, Long> {
    // 특정 리뷰의 댓글 목록 조회 (작성일 오름차순)
    fun findByReviewReviewIdOrderByCreatedAtAsc(reviewId: Long): List<Comment>

    // 특정 회원이 작성한 댓글 목록 조회
    fun findByMemberIdOrderByCreatedAtDesc(memberId: Long): List<Comment>

    // 특정 리뷰의 댓글 수 조회
    fun countByReviewReviewId(reviewId: Long): Long
}