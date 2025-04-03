package com.tripfriend.domain.review.controller;

import com.tripfriend.domain.member.member.entity.Member;
import com.tripfriend.domain.member.member.service.AuthService;
import com.tripfriend.domain.member.member.repository.MemberRepository;
import com.tripfriend.domain.review.dto.CommentRequestDto;
import com.tripfriend.domain.review.dto.CommentResponseDto;
import com.tripfriend.domain.review.service.CommentService;
import com.tripfriend.global.dto.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.tripfriend.global.exception.ServiceException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Comment API", description = "댓글 관련 기능을 제공합니다.")
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final AuthService authService;

    // 댓글 생성
    @Operation(summary = "댓글 생성")
    @PostMapping
    public RsData<CommentResponseDto> createComment(
            @Valid @RequestBody CommentRequestDto requestDto,
            @RequestHeader(value = "Authorization", required = false) String token) {

        // 토큰에서 인증된 사용자 정보 가져오기
        Member loggedInMember = authService.getLoggedInMember(token);

        CommentResponseDto responseDto = commentService.createComment(requestDto, loggedInMember);
        return new RsData<>("201-1", "댓글이 성공적으로 등록되었습니다.", responseDto);
    }

    // 댓글 상세 조회
    @Operation(summary = "댓글 상세 조회")
    @GetMapping("/{commentId}")
    public RsData<CommentResponseDto> getComment(@PathVariable("commentId") Long commentId) {
        CommentResponseDto responseDto = commentService.getComment(commentId);
        return new RsData<>("200-1", "댓글 조회에 성공했습니다.", responseDto);
    }

    // 특정 리뷰의 댓글 목록 조회
    @Operation(summary = "특정 리뷰의 댓글 목록 조회")
    @GetMapping("/review/{reviewId}")
    public RsData<List<CommentResponseDto>> getCommentsByReview(@PathVariable("reviewId") Long reviewId) {
        List<CommentResponseDto> responseDtoList = commentService.getCommentsByReview(reviewId);
        return new RsData<>("200-2", "리뷰의 댓글 목록을 성공적으로 조회했습니다.", responseDtoList);
    }

    // 내가 작성한 댓글 목록 조회
    @Operation(summary = "내가 작성한 댓글 목록 조회")
    @GetMapping("/my")
    public RsData<List<CommentResponseDto>> getMyComments(
            @RequestHeader(value = "Authorization", required = false) String token) {

        // 토큰에서 인증된 사용자 정보 가져오기
        Member loggedInMember = authService.getLoggedInMember(token);

        List<CommentResponseDto> responseDtoList = commentService.getCommentsByMember(loggedInMember.getId());
        return new RsData<>("200-3", "내 댓글 목록을 성공적으로 조회했습니다.", responseDtoList);
    }

    // 댓글 수정
    @Operation(summary = "댓글 수정")
    @PutMapping("/{commentId}")
    public RsData<CommentResponseDto> updateComment(
            @PathVariable("commentId") Long commentId,
            @Valid @RequestBody CommentRequestDto requestDto,
            @RequestHeader(value = "Authorization", required = false) String token) {

        // 토큰에서 인증된 사용자 정보 가져오기
        Member loggedInMember = authService.getLoggedInMember(token);

        CommentResponseDto responseDto = commentService.updateComment(commentId, requestDto, loggedInMember);
        return new RsData<>("200-4", "댓글이 성공적으로 수정되었습니다.", responseDto);
    }

    // 댓글 삭제
    @Operation(summary = "댓글 삭제")
    @DeleteMapping("/{commentId}")
    public RsData<Void> deleteComment(
            @PathVariable("commentId") Long commentId,
            @RequestHeader(value = "Authorization", required = false) String token) {

        // 토큰에서 인증된 사용자 정보 가져오기
        Member loggedInMember = authService.getLoggedInMember(token);

        commentService.deleteComment(commentId, loggedInMember);
        return new RsData<>("200-5", "댓글이 성공적으로 삭제되었습니다.");
    }
}