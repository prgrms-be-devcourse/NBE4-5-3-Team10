package com.tripfriend.domain.review.service

import com.tripfriend.domain.member.member.entity.Member
import com.tripfriend.domain.review.dto.CommentRequestDto
import com.tripfriend.domain.review.dto.CommentResponseDto
import com.tripfriend.domain.review.entity.Comment
import com.tripfriend.domain.review.repository.CommentRepository
import com.tripfriend.domain.review.repository.ReviewRepository
import com.tripfriend.global.exception.ServiceException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class CommentService(
    private val commentRepository: CommentRepository,
    private val reviewRepository: ReviewRepository
) {

    // 댓글 생성
    @Transactional
    fun createComment(requestDto: CommentRequestDto, member: Member): CommentResponseDto {
        // 댓글을 달 리뷰 조회
        val review = reviewRepository.findById(requestDto.reviewId!!)
            .orElseThrow { ServiceException("404-3", "존재하지 않는 리뷰입니다.") }

        // 새 댓글 엔티티 생성
        val comment = Comment(
            requestDto.content,
            review,
            member
        )

        // 댓글 저장
        val savedComment = commentRepository.save(comment)

        // 저장된 댓글을 DTO로 변환하여 반환
        return CommentResponseDto(savedComment, member.nickname)
    }

    // 특정 리뷰의 댓글 목록 조회
    fun getCommentsByReview(reviewId: Long): List<CommentResponseDto> {
        // 리뷰 존재 여부 확인
        if (!reviewRepository.existsById(reviewId)) {
            throw ServiceException("404-3", "존재하지 않는 리뷰입니다.")
        }

        val comments = commentRepository.findByReviewReviewIdOrderByCreatedAtAsc(reviewId)

        // 댓글 목록을 DTO 목록으로 변환하여 반환
        return comments.map { comment ->
            CommentResponseDto(comment, comment.member?.nickname ?: "", comment.member?.profileImage)
        }
    }

    // 특정 회원의 댓글 목록 조회
    fun getCommentsByMember(memberId: Long): List<CommentResponseDto> {
        val comments = commentRepository.findByMemberIdOrderByCreatedAtDesc(memberId)

        // 댓글 목록을 DTO 목록으로 변환하여 반환
        return comments.map { comment ->
            CommentResponseDto(comment, comment.member?.nickname ?: "", comment.member?.profileImage)
        }
    }

    // 댓글 상세 조회
    fun getComment(commentId: Long): CommentResponseDto {
        val comment = commentRepository.findById(commentId)
            .orElseThrow { ServiceException("404-4", "존재하지 않는 댓글입니다.") }

        return CommentResponseDto(comment, comment.member?.nickname ?: "")
    }

    // 댓글 수정
    @Transactional
    fun updateComment(commentId: Long, requestDto: CommentRequestDto, member: Member): CommentResponseDto {
        val comment = commentRepository.findById(commentId)
            .orElseThrow { ServiceException("404-4", "존재하지 않는 댓글입니다.") }

        // 작성자 본인인지 확인
        if (comment.member?.id != member.id) {
            throw ServiceException("403-2", "댓글 작성자만 수정할 수 있습니다.")
        }

        // 댓글 내용 업데이트
        comment.update(requestDto.content)

        // 업데이트된 댓글을 DTO로 변환하여 반환
        return CommentResponseDto(comment, member.nickname)
    }

    // 댓글 삭제
    @Transactional
    fun deleteComment(commentId: Long, member: Member) {
        val comment = commentRepository.findById(commentId)
            .orElseThrow { ServiceException("404-4", "존재하지 않는 댓글입니다.") }

        // 작성자 본인인지 확인
        if (comment.member?.id != member.id) {
            throw ServiceException("403-2", "댓글 작성자만 삭제할 수 있습니다.")
        }

        // 댓글 삭제
        commentRepository.delete(comment)
    }
}