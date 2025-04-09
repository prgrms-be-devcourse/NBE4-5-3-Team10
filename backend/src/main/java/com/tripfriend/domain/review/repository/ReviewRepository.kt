package com.tripfriend.domain.review.repository

import com.tripfriend.domain.review.entity.Review
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ReviewRepository : JpaRepository<Review, Long> {

    // 댓글 수를 기준으로 리뷰 정렬 (많은 순)
    @Query("SELECT r, COUNT(c) as commentCount FROM Review r LEFT JOIN Comment c ON r.reviewId = c.review.reviewId GROUP BY r ORDER BY commentCount DESC")
    fun findAllOrderByCommentCountDesc(): List<Array<Any>>

    // 평점 높은 순으로 정렬
    fun findAllByOrderByRatingDesc(): List<Review>

    // 평점 낮은 순으로 정렬
    fun findAllByOrderByRatingAsc(): List<Review>

    // 최신순 정렬 - 기본값
    fun findAllByOrderByCreatedAtDesc(): List<Review>

    // 오래된순 정렬
    fun findAllByOrderByCreatedAtAsc(): List<Review>

    // 제목으로 검색
    fun findByTitleContainingOrderByCreatedAtDesc(keyword: String): List<Review>

    // 여행지 ID로 검색
    fun findByPlace_IdOrderByCreatedAtDesc(placeId: Long): List<Review>

    // 여행지 ID + 정렬 옵션 (평점 높은 순)
    fun findByPlace_IdOrderByRatingDesc(placeId: Long): List<Review>

    // 여행지 ID + 정렬 옵션 (평점 낮은 순)
    fun findByPlace_IdOrderByRatingAsc(placeId: Long): List<Review>

    // 특정 사용자의 리뷰 목록
    fun findByMemberIdOrderByCreatedAtDesc(memberId: Long): List<Review>
}