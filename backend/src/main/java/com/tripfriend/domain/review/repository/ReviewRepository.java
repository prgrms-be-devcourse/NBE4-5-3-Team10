package com.tripfriend.domain.review.repository;

import com.tripfriend.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 댓글 수를 기준으로 리뷰 정렬 (많은 순)
    @Query("SELECT r, COUNT(c) as commentCount FROM Review r LEFT JOIN Comment c ON r.reviewId = c.review.reviewId GROUP BY r ORDER BY commentCount DESC")
    List<Object[]> findAllOrderByCommentCountDesc();

    // 평점 높은 순으로 정렬
    List<Review> findAllByOrderByRatingDesc();

    // 평점 낮은 순으로 정렬
    List<Review> findAllByOrderByRatingAsc();

    // 최신순 정렬 - 기본값
    List<Review> findAllByOrderByCreatedAtDesc();

    // 오래된순 정렬
    List<Review> findAllByOrderByCreatedAtAsc();

    // 제목으로 검색
    List<Review> findByTitleContainingOrderByCreatedAtDesc(String keyword);

    // 여행지 ID로 검색
    List<Review> findByPlace_IdOrderByCreatedAtDesc(Long placeId);

    // 여행지 ID + 정렬 옵션 (평점 높은 순)
    List<Review> findByPlace_IdOrderByRatingDesc(Long placeId);

    // 여행지 ID + 정렬 옵션 (평점 낮은 순)
    List<Review> findByPlace_IdOrderByRatingAsc(Long placeId);

    // 특정 사용자의 리뷰 목록
    List<Review> findByMemberIdOrderByCreatedAtDesc(Long memberId);
}