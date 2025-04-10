package com.tripfriend.domain.review.repository

import com.tripfriend.domain.review.entity.Review
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ReviewRepository : JpaRepository<Review, Long> {

    // 특정 여행지 - 댓글 많은순 -> 최신순 정렬
    @Query("""
        SELECT r 
        FROM Review r 
        LEFT JOIN Comment c ON r.reviewId = c.review.reviewId 
        WHERE r.place.id = :placeId 
        GROUP BY r 
        ORDER BY COUNT(c) DESC, r.createdAt DESC
    """)
    fun findByPlace_IdOrderByCommentCountDesc(placeId: Long): List<Review>

    // 특정 여행지 - 평점 높은순 -> 조회수 많은순 -> 최신순
    @Query("""
        SELECT r 
        FROM Review r
        LEFT JOIN ReviewViewCount vc ON r.reviewId = vc.reviewId
        WHERE r.place.id = :placeId 
        ORDER BY r.rating DESC, vc.count DESC, r.createdAt DESC
    """)
    fun findByPlace_IdOrderByRatingDesc(placeId: Long): List<Review>

    // 특정 여행지 - 평점 낮은순 -> 조회수 많은순 -> 최신순
    @Query("""
        SELECT r 
        FROM Review r
        LEFT JOIN ReviewViewCount vc ON r.reviewId = vc.reviewId
        WHERE r.place.id = :placeId 
        ORDER BY r.rating ASC, vc.count DESC, r.createdAt DESC
    """)
    fun findByPlace_IdOrderByRatingAsc(placeId: Long): List<Review>

    // 특정 여행지 - 최신순
    fun findByPlace_IdOrderByCreatedAtDesc(placeId: Long): List<Review>

    // 특정 여행지 - 오래된순
    fun findByPlace_IdOrderByCreatedAtAsc(placeId: Long): List<Review>

    // 전체 - 평점 높은순 -> 조회수 많은순 -> 최신순
    @Query("""
        SELECT r 
        FROM Review r
        LEFT JOIN ReviewViewCount vc ON r.reviewId = vc.reviewId
        ORDER BY r.rating DESC, vc.count DESC, r.createdAt DESC
    """)
    fun findAllByOrderByRatingDesc(): List<Review>

    // 전체 - 평점 낮은순 -> 조회수 많은순 -> 최신순
    @Query("""
        SELECT r 
        FROM Review r
        LEFT JOIN ReviewViewCount vc ON r.reviewId = vc.reviewId
        ORDER BY r.rating ASC, vc.count DESC, r.createdAt DESC
    """)
    fun findAllByOrderByRatingAsc(): List<Review>

    // 전체 - 최신순
    fun findAllByOrderByCreatedAtDesc(): List<Review>

    // 전체 - 오래된순
    fun findAllByOrderByCreatedAtAsc(): List<Review>

    // keyword 포함된 리뷰를 최신순으로 정렬
    fun findByTitleContainingOrderByCreatedAtDesc(keyword: String): List<Review>

    // 특정 멤버의 리뷰 목록 최신순
    fun findByMemberIdOrderByCreatedAtDesc(memberId: Long): List<Review>

    // 전체 리뷰 댓글 많은순 → 최신순 정렬
    @Query("""
        SELECT r, COUNT(c) as commentCount 
        FROM Review r 
        LEFT JOIN Comment c ON r.reviewId = c.review.reviewId 
        GROUP BY r 
        ORDER BY commentCount DESC, r.createdAt DESC
    """)
    fun findAllOrderByCommentCountDesc(): List<Array<Any>>
}