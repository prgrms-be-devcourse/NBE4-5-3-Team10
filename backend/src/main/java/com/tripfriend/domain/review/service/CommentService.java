package com.tripfriend.domain.review.service;

import com.tripfriend.domain.member.member.entity.Member;
import com.tripfriend.domain.review.dto.CommentRequestDto;
import com.tripfriend.domain.review.dto.CommentResponseDto;
import com.tripfriend.domain.review.entity.Comment;
import com.tripfriend.domain.review.entity.Review;
import com.tripfriend.domain.review.repository.CommentRepository;
import com.tripfriend.domain.review.repository.ReviewRepository;
import com.tripfriend.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final ReviewRepository reviewRepository;

    // 댓글 생성
    @Transactional
    public CommentResponseDto createComment(CommentRequestDto requestDto, Member member) {
        // 댓글을 달 리뷰 조회
        Review review = reviewRepository.findById(requestDto.getReviewId())
                .orElseThrow(() -> new ServiceException("404-3", "존재하지 않는 리뷰입니다."));

        // 새 댓글 엔티티 생성
        Comment comment = new Comment(
                requestDto.getContent(),
                review,
                member
        );

        // 댓글 저장
        Comment savedComment = commentRepository.save(comment);

        // 저장된 댓글을 DTO로 변환하여 반환
        return new CommentResponseDto(savedComment, member.getNickname());
    }

    // 특정 리뷰의 댓글 목록 조회
    public List<CommentResponseDto> getCommentsByReview(Long reviewId) {
        // 리뷰 존재 여부 확인
        if (!reviewRepository.existsById(reviewId)) {
            throw new ServiceException("404-3", "존재하지 않는 리뷰입니다.");
        }

        List<Comment> comments = commentRepository.findByReviewReviewIdOrderByCreatedAtAsc(reviewId);

        // 댓글 목록을 DTO 목록으로 변환하여 반환
        return comments.stream()
                .map(comment -> new CommentResponseDto(comment, comment.getMember().getNickname(), comment.getMember().getProfileImage()))
                .collect(Collectors.toList());
    }

    // 특정 회원의 댓글 목록 조회
    public List<CommentResponseDto> getCommentsByMember(Long memberId) {
        List<Comment> comments = commentRepository.findByMemberIdOrderByCreatedAtDesc(memberId);

        // 댓글 목록을 DTO 목록으로 변환하여 반환
        return comments.stream()
                .map(comment -> new CommentResponseDto(comment, comment.getMember().getNickname(), comment.getMember().getProfileImage()))
                .collect(Collectors.toList());
    }

    // 댓글 상세 조회
    public CommentResponseDto getComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ServiceException("404-4", "존재하지 않는 댓글입니다."));

        return new CommentResponseDto(comment, comment.getMember().getNickname());
    }

    // 댓글 수정
    @Transactional
    public CommentResponseDto updateComment(Long commentId, CommentRequestDto requestDto, Member member) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ServiceException("404-4", "존재하지 않는 댓글입니다."));

        // 작성자 본인인지 확인
        if (!comment.getMember().getId().equals(member.getId())) {
            throw new ServiceException("403-2", "댓글 작성자만 수정할 수 있습니다.");
        }

        // 댓글 내용 업데이트
        comment.update(requestDto.getContent());

        // 업데이트된 댓글을 DTO로 변환하여 반환
        return new CommentResponseDto(comment, member.getNickname());
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(Long commentId, Member member) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ServiceException("404-4", "존재하지 않는 댓글입니다."));

        // 작성자 본인인지 확인
        if (!comment.getMember().getId().equals(member.getId())) {
            throw new ServiceException("403-2", "댓글 작성자만 삭제할 수 있습니다.");
        }

        // 댓글 삭제
        commentRepository.delete(comment);
    }
}