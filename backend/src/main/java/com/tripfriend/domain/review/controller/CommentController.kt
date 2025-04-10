package com.tripfriend.domain.review.controller

import com.tripfriend.domain.member.member.service.AuthService
import com.tripfriend.domain.review.dto.CommentRequestDto
import com.tripfriend.domain.review.dto.CommentResponseDto
import com.tripfriend.domain.review.service.CommentService
import com.tripfriend.global.dto.RsData
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@Tag(name = "Comment API", description = "댓글 관련 기능을 제공합니다.")
@RestController
@RequestMapping("/api/comments")
class CommentController(
    private val commentService: CommentService,
    private val authService: AuthService
) {

    // 댓글 생성
    @Operation(summary = "댓글 생성")
    @PostMapping
    fun createComment(
        @Valid @RequestBody requestDto: CommentRequestDto,
        @RequestHeader(value = "Authorization", required = false) token: String?
    ): RsData<CommentResponseDto> {
        // 토큰에서 인증된 사용자 정보 가져오기
        val loggedInMember = authService.getLoggedInMember(token!!)

        val responseDto = commentService.createComment(requestDto, loggedInMember)
        return RsData("201-1", "댓글이 성공적으로 등록되었습니다.", responseDto)
    }

    // 댓글 상세 조회
    @Operation(summary = "댓글 상세 조회")
    @GetMapping("/{commentId}")
    fun getComment(@PathVariable("commentId") commentId: Long): RsData<CommentResponseDto> {
        val responseDto = commentService.getComment(commentId)
        return RsData("200-1", "댓글 조회에 성공했습니다.", responseDto)
    }

    // 특정 리뷰의 댓글 목록 조회
    @Operation(summary = "특정 리뷰의 댓글 목록 조회")
    @GetMapping("/review/{reviewId}")
    fun getCommentsByReview(@PathVariable("reviewId") reviewId: Long): RsData<List<CommentResponseDto>> {
        val responseDtoList = commentService.getCommentsByReview(reviewId)
        return RsData("200-2", "리뷰의 댓글 목록을 성공적으로 조회했습니다.", responseDtoList)
    }

    // 내가 작성한 댓글 목록 조회
    @Operation(summary = "내가 작성한 댓글 목록 조회")
    @GetMapping("/my")
    fun getMyComments(
        @RequestHeader(value = "Authorization", required = false) token: String?
    ): RsData<List<CommentResponseDto>> {
        // 토큰에서 인증된 사용자 정보 가져오기
        val loggedInMember = authService.getLoggedInMember(token!!)

        val responseDtoList = commentService.getCommentsByMember(loggedInMember.id!!)
        return RsData("200-3", "내 댓글 목록을 성공적으로 조회했습니다.", responseDtoList)
    }

    // 댓글 수정
    @Operation(summary = "댓글 수정")
    @PutMapping("/{commentId}")
    fun updateComment(
        @PathVariable("commentId") commentId: Long,
        @Valid @RequestBody requestDto: CommentRequestDto,
        @RequestHeader(value = "Authorization", required = false) token: String?
    ): RsData<CommentResponseDto> {
        // 토큰에서 인증된 사용자 정보 가져오기
        val loggedInMember = authService.getLoggedInMember(token!!)

        val responseDto = commentService.updateComment(commentId, requestDto, loggedInMember)
        return RsData("200-4", "댓글이 성공적으로 수정되었습니다.", responseDto)
    }

    // 댓글 삭제
    @Operation(summary = "댓글 삭제")
    @DeleteMapping("/{commentId}")
    fun deleteComment(
        @PathVariable("commentId") commentId: Long,
        @RequestHeader(value = "Authorization", required = false) token: String?
    ): RsData<Void> {
        // 토큰에서 인증된 사용자 정보 가져오기
        val loggedInMember = authService.getLoggedInMember(token!!)

        commentService.deleteComment(commentId, loggedInMember)
        return RsData("200-5", "댓글이 성공적으로 삭제되었습니다.")
    }
}