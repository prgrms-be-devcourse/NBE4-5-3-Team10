package com.tripfriend.domain.review.controller

import com.tripfriend.domain.member.member.service.AuthService
import com.tripfriend.domain.review.dto.ReviewRequestDto
import com.tripfriend.domain.review.dto.ReviewResponseDto
import com.tripfriend.domain.review.service.ReviewService
import com.tripfriend.global.dto.RsData
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpSession
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@Tag(name = "Review API", description = "리뷰 관련 기능을 제공합니다.")
@RestController
@RequestMapping("/api/reviews")
class ReviewController(
    private val reviewService: ReviewService,
    private val authService: AuthService
) {

    // 리뷰 생성
    @Operation(summary = "리뷰 생성")
    @PostMapping
    fun createReview(
        @Valid @RequestBody requestDto: ReviewRequestDto,
        @RequestHeader(value = "Authorization", required = false) token: String?
    ): RsData<ReviewResponseDto> {
        // 토큰에서 인증된 사용자 정보 가져오기
        val loggedInMember = authService.getLoggedInMember(token!!)

        val responseDto = reviewService.createReview(requestDto, loggedInMember)
        return RsData("201-1", "리뷰가 성공적으로 등록되었습니다.", responseDto)
    }

    // 리뷰 상세 조회
    @Operation(summary = "리뷰 상세 조회")
    @GetMapping("/{reviewId}")
    fun getReview(
        @PathVariable("reviewId") reviewId: Long,
        session: HttpSession
    ): RsData<ReviewResponseDto> {
        // 세션에서 조회한 리뷰 목록 가져오기
        @Suppress("UNCHECKED_CAST")
        var viewedReviews = session.getAttribute("VIEWED_REVIEWS") as MutableSet<Long>?

        // 세션에 조회 목록이 없으면 새로 생성
        if (viewedReviews == null) {
            viewedReviews = mutableSetOf()
            session.setAttribute("VIEWED_REVIEWS", viewedReviews)
        }

        // 이미 조회한 리뷰가 아닐 경우에만 조회수 증가
        val isNewView = viewedReviews.add(reviewId)

        val responseDto = reviewService.getReview(reviewId, isNewView)
        return RsData("200-1", "리뷰 조회에 성공했습니다.", responseDto)
    }

    // 특정 장소의 리뷰 목록 조회
    @Operation(summary = "특정 장소의 리뷰 목록 조회")
    @GetMapping("/place/{placeId}")
    fun getReviewsByPlace(@PathVariable("placeId") placeId: Long): RsData<List<ReviewResponseDto>> {
        val responseDtoList = reviewService.getReviewsByPlace(placeId)
        return RsData("200-2", "장소의 리뷰 목록을 성공적으로 조회했습니다.", responseDtoList)
    }

    // 리뷰 수정
    @Operation(summary = "리뷰 수정")
    @PutMapping("/{reviewId}")
    fun updateReview(
        @PathVariable("reviewId") reviewId: Long,
        @Valid @RequestBody requestDto: ReviewRequestDto,
        @RequestHeader(value = "Authorization", required = false) token: String?
    ): RsData<ReviewResponseDto> {
        // 토큰에서 인증된 사용자 정보 가져오기
        val loggedInMember = authService.getLoggedInMember(token!!)

        val responseDto = reviewService.updateReview(reviewId, requestDto, loggedInMember)
        return RsData("200-3", "리뷰가 성공적으로 수정되었습니다.", responseDto)
    }

    // 리뷰 삭제
    @Operation(summary = "리뷰 삭제")
    @DeleteMapping("/{reviewId}")
    fun deleteReview(
        @PathVariable("reviewId") reviewId: Long,
        @RequestHeader(value = "Authorization", required = false) token: String?
    ): RsData<Void> {
        // 토큰에서 인증된 사용자 정보 가져오기
        val loggedInMember = authService.getLoggedInMember(token!!)

        reviewService.deleteReview(reviewId, loggedInMember)
        return RsData("200-4", "리뷰가 성공적으로 삭제되었습니다.")
    }

    // 인기 게시물 조회
    @Operation(summary = "인기 게시물 조회")
    @GetMapping("/popular")
    fun getPopularReviews(
        @RequestParam(name = "limit", defaultValue = "10") limit: Int
    ): RsData<List<ReviewResponseDto>> {
        val popularReviews = reviewService.getPopularReviews(limit)
        return RsData("200-6", "인기 리뷰 목록을 성공적으로 조회했습니다.", popularReviews)
    }

    // 리뷰 목록 조회 (정렬 및 검색)
    @Operation(summary = "리뷰 목록 조회 (정렬 및 검색)")
    @GetMapping
    fun getReviews(
        @RequestParam(name = "sort", defaultValue = "newest") sort: String,
        @RequestParam(name = "keyword", required = false) keyword: String?,
        @RequestParam(name = "placeId", required = false) placeId: Long?,
        @RequestHeader(value = "Authorization", required = false) token: String?
    ): RsData<List<ReviewResponseDto>> {
        // 항상 전체 리뷰를 조회 (memberId null)
        val reviews = reviewService.getReviews(sort, keyword, placeId, null)
        return RsData("200-5", "리뷰 목록을 성공적으로 조회했습니다.", reviews)
    }

    // 내가 작성한 리뷰 목록 조회
    @Operation(summary = "내가 작성한 리뷰 목록 조회")
    @GetMapping("/my")
    fun getMyReviews(
        @RequestHeader(value = "Authorization", required = false) token: String?
    ): RsData<List<ReviewResponseDto>> {
        // 토큰에서 인증된 사용자 정보 가져오기
        val loggedInMember = authService.getLoggedInMember(token!!)

        val reviews = reviewService.getReviewsByMember(loggedInMember.id)
        return RsData("200-7", "내 리뷰 목록을 성공적으로 조회했습니다.", reviews)
    }

    // 특정 회원의 리뷰 목록 조회
    @Operation(summary = "특정 회원의 리뷰 목록 조회")
    @GetMapping("/member/{memberId}")
    fun getMemberReviews(
        @PathVariable("memberId") memberId: Long
    ): RsData<List<ReviewResponseDto>> {
        val reviews = reviewService.getReviewsByMember(memberId)
        return RsData("200-8", "회원의 리뷰 목록을 성공적으로 조회했습니다.", reviews)
    }
}