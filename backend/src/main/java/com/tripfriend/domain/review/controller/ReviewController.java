package com.tripfriend.domain.review.controller;

import com.tripfriend.domain.member.member.entity.Member;
import com.tripfriend.domain.member.member.service.AuthService;
import com.tripfriend.domain.member.member.repository.MemberRepository;
import com.tripfriend.domain.review.dto.ReviewRequestDto;
import com.tripfriend.domain.review.dto.ReviewResponseDto;
import com.tripfriend.domain.review.service.ReviewService;
import com.tripfriend.global.dto.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.tripfriend.global.exception.ServiceException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Tag(name = "Review API", description = "리뷰 관련 기능을 제공합니다.")
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final AuthService authService;

    // 리뷰 생성
    @Operation(summary = "리뷰 생성")
    @PostMapping
    public RsData<ReviewResponseDto> createReview(
            @Valid @RequestBody ReviewRequestDto requestDto,
            @RequestHeader(value = "Authorization", required = false) String token) {

        // 토큰에서 인증된 사용자 정보 가져오기
        Member loggedInMember = authService.getLoggedInMember(token);

        ReviewResponseDto responseDto = reviewService.createReview(requestDto, loggedInMember);
        return new RsData<>("201-1", "리뷰가 성공적으로 등록되었습니다.", responseDto);
    }

    // 리뷰 상세 조회
    @Operation(summary = "리뷰 상세 조회")
    @GetMapping("/{reviewId}")
    public RsData<ReviewResponseDto> getReview(
            @PathVariable("reviewId") Long reviewId,
            HttpSession session) {

        // 세션에서 조회한 리뷰 목록 가져오기
        Set<Long> viewedReviews = (Set<Long>) session.getAttribute("VIEWED_REVIEWS");

        // 세션에 조회 목록이 없으면 새로 생성
        if (viewedReviews == null) {
            viewedReviews = new HashSet<>();
            session.setAttribute("VIEWED_REVIEWS", viewedReviews);
        }

        // 이미 조회한 리뷰가 아닐 경우에만 조회수 증가
        boolean isNewView = viewedReviews.add(reviewId);

        ReviewResponseDto responseDto = reviewService.getReview(reviewId, isNewView);
        return new RsData<>("200-1", "리뷰 조회에 성공했습니다.", responseDto);
    }

    // 특정 장소의 리뷰 목록 조회
    @Operation(summary = "특정 장소의 리뷰 목록 조회")
    @GetMapping("/place/{placeId}")
    public RsData<List<ReviewResponseDto>> getReviewsByPlace(@PathVariable("placeId") Long placeId) {
        List<ReviewResponseDto> responseDtoList = reviewService.getReviewsByPlace(placeId);
        return new RsData<>("200-2", "장소의 리뷰 목록을 성공적으로 조회했습니다.", responseDtoList);
    }

    // 리뷰 수정
    @Operation(summary = "리뷰 수정")
    @PutMapping("/{reviewId}")
    public RsData<ReviewResponseDto> updateReview(
            @PathVariable("reviewId") Long reviewId,
            @Valid @RequestBody ReviewRequestDto requestDto,
            @RequestHeader(value = "Authorization", required = false) String token) {

        // 토큰에서 인증된 사용자 정보 가져오기
        Member loggedInMember = authService.getLoggedInMember(token);

        ReviewResponseDto responseDto = reviewService.updateReview(reviewId, requestDto, loggedInMember);
        return new RsData<>("200-3", "리뷰가 성공적으로 수정되었습니다.", responseDto);
    }


    // 리뷰 삭제
    @Operation(summary = "리뷰 삭제")
    @DeleteMapping("/{reviewId}")
    public RsData<Void> deleteReview(
            @PathVariable("reviewId") Long reviewId,
            @RequestHeader(value = "Authorization", required = false) String token) {

        // 토큰에서 인증된 사용자 정보 가져오기
        Member loggedInMember = authService.getLoggedInMember(token);

        reviewService.deleteReview(reviewId, loggedInMember);
        return new RsData<>("200-4", "리뷰가 성공적으로 삭제되었습니다.");
    }


    // 인기 게시물 조회
    @Operation(summary = "인기 게시물 조회")
    @GetMapping("/popular")
    public RsData<List<ReviewResponseDto>> getPopularReviews(
            @RequestParam(name = "limit", defaultValue = "10") int limit) {

        List<ReviewResponseDto> popularReviews = reviewService.getPopularReviews(limit);
        return new RsData<>("200-6", "인기 리뷰 목록을 성공적으로 조회했습니다.", popularReviews);
    }


    // 리뷰 목록 조회 (정렬 및 검색)
    @Operation(summary = "리뷰 목록 조회 (정렬 및 검색)")
    @GetMapping
    public RsData<List<ReviewResponseDto>> getReviews(
            @RequestParam(name = "sort", defaultValue = "newest") String sort,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "placeId", required = false) Long placeId,
            @RequestHeader(value = "Authorization", required = false) String token) {

        // 항상 전체 리뷰를 조회 (memberId null)
        List<ReviewResponseDto> reviews = reviewService.getReviews(sort, keyword, placeId, null);
        return new RsData<>("200-5", "리뷰 목록을 성공적으로 조회했습니다.", reviews);
    }


    // 내가 작성한 리뷰 목록 조회
    @Operation(summary = "내가 작성한 리뷰 목록 조회")
    @GetMapping("/my")
    public RsData<List<ReviewResponseDto>> getMyReviews(
            @RequestHeader(value = "Authorization", required = false) String token) {

        // 토큰에서 인증된 사용자 정보 가져오기
        Member loggedInMember = authService.getLoggedInMember(token);

        List<ReviewResponseDto> reviews = reviewService.getReviewsByMember(loggedInMember.getId());
        return new RsData<>("200-7", "내 리뷰 목록을 성공적으로 조회했습니다.", reviews);
    }

    // 특정 회원의 리뷰 목록 조회
    @Operation(summary = "특정 회원의 리뷰 목록 조회")
    @GetMapping("/member/{memberId}")
    public RsData<List<ReviewResponseDto>> getMemberReviews(
            @PathVariable("memberId") Long memberId) {

        List<ReviewResponseDto> reviews = reviewService.getReviewsByMember(memberId);
        return new RsData<>("200-8", "회원의 리뷰 목록을 성공적으로 조회했습니다.", reviews);
    }
}