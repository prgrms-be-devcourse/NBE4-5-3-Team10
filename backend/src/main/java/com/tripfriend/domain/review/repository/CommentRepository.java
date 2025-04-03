package com.tripfriend.domain.review.repository;

import com.tripfriend.domain.review.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    // 특정 리뷰의 댓글 목록 조회 (작성일 오름차순)
    List<Comment> findByReviewReviewIdOrderByCreatedAtAsc(Long reviewId);

    // 특정 회원이 작성한 댓글 목록 조회
    List<Comment> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    // 특정 리뷰의 댓글 수 조회
    long countByReviewReviewId(Long reviewId);
}
