package com.tripfriend.domain.review.service;

import com.tripfriend.domain.member.member.entity.Member;
import com.tripfriend.domain.place.place.entity.Place;
import com.tripfriend.domain.place.place.repository.PlaceRepository;
import com.tripfriend.domain.review.dto.ReviewRequestDto;
import com.tripfriend.domain.review.dto.ReviewResponseDto;
import com.tripfriend.domain.review.entity.Review;
import com.tripfriend.domain.review.entity.ReviewViewCount;
import com.tripfriend.domain.review.repository.CommentRepository;
import com.tripfriend.domain.review.repository.ReviewRepository;
import com.tripfriend.domain.review.repository.ReviewViewCountRepository;
import com.tripfriend.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final CommentRepository commentRepository;
    private final ReviewViewCountRepository viewCountRepository;

    // ★ 추가: placeRepository 주입
    private final PlaceRepository placeRepository;

    // 리뷰 생성
    @Transactional
    public ReviewResponseDto createReview(ReviewRequestDto requestDto, Member member) {
        // 유효성 검증
        if (requestDto.getPlaceId() == null) {
            throw new ServiceException("400-1", "여행지 정보는 필수입니다.");
        }
        if (requestDto.getRating() < 1 || requestDto.getRating() > 5) {
            throw new ServiceException("400-2", "평점은 1점에서 5점 사이여야 합니다.");
        }

        // placeId로 실제 Place 엔티티를 조회
        Place place = placeRepository.findById(requestDto.getPlaceId())
                .orElseThrow(() -> new ServiceException("404-2", "해당 여행지가 존재하지 않습니다."));

        // Review 생성자에 place 객체 주입
        Review review = new Review(
                requestDto.getTitle(),
                requestDto.getContent(),
                requestDto.getRating(),
                member,
                place
        );

        // 리뷰 저장
        Review savedReview = reviewRepository.save(review);

        // 조회수 엔티티 초기화
        ReviewViewCount viewCount = new ReviewViewCount(savedReview);
        viewCountRepository.save(viewCount);

        // 댓글 수는 0이므로 그대로 DTO 반환
        return new ReviewResponseDto(savedReview, member.getNickname(), 0);
    }

    // 리뷰 상세 조회
    @Transactional
    public ReviewResponseDto getReview(Long reviewId, boolean incrementView) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ServiceException("404-1", "존재하지 않는 리뷰입니다."));

        // 조회수 처리
        ReviewViewCount viewCount = viewCountRepository.findById(reviewId)
                .orElseGet(() -> {
                    ReviewViewCount newViewCount = new ReviewViewCount(review);
                    return viewCountRepository.save(newViewCount);
                });

        // 새로운 조회일 경우에만 조회수 증가
        if (incrementView) {
            viewCount.increment();
            viewCountRepository.save(viewCount);
        }

        // 댓글 수 조회
        int commentCount = commentRepository.findByReviewReviewIdOrderByCreatedAtAsc(reviewId).size();

        // 응답 DTO 생성
        ReviewResponseDto responseDto = new ReviewResponseDto(review, review.getMember().getNickname(), commentCount, review.getMember().getProfileImage());
        responseDto.setViewCount(viewCount.getCount());

        return responseDto;
    }

    // 원본 메서드(오버로딩)
    @Transactional
    public ReviewResponseDto getReview(Long reviewId) {
        return getReview(reviewId, true);
    }

    // 특정 장소의 리뷰 목록 조회
    public List<ReviewResponseDto> getReviewsByPlace(Long placeId) {
        if (placeId == null) {
            throw new ServiceException("400-3", "여행지 ID는 필수입니다.");
        }

        // placeId 대신 place.id를 기준으로 검색
        List<Review> reviews = reviewRepository.findByPlace_IdOrderByCreatedAtDesc(placeId);

        return reviews.stream()
                .map(review -> {
                    int commentCount = commentRepository.findByReviewReviewIdOrderByCreatedAtAsc(review.getReviewId()).size();
                    ReviewResponseDto dto = new ReviewResponseDto(review, review.getMember().getNickname(), commentCount);

                    // 조회수 설정
                    viewCountRepository.findById(review.getReviewId())
                            .ifPresent(vc -> dto.setViewCount(vc.getCount()));

                    return dto;
                })
                .collect(Collectors.toList());
    }

    // 리뷰 수정
    @Transactional
    public ReviewResponseDto updateReview(Long reviewId, ReviewRequestDto requestDto, Member member) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ServiceException("404-1", "존재하지 않는 리뷰입니다."));

        // 작성자 본인인지 확인
        if (!review.getMember().getId().equals(member.getId())) {
            throw new ServiceException("403-1", "리뷰 작성자만 수정할 수 있습니다.");
        }

        // 평점 유효성 검증
        if (requestDto.getRating() < 1 || requestDto.getRating() > 5) {
            throw new ServiceException("400-2", "평점은 1점에서 5점 사이여야 합니다.");
        }

        // 내용 업데이트
        review.update(requestDto.getTitle(), requestDto.getContent(), requestDto.getRating());

        // 댓글 수
        int commentCount = commentRepository.findByReviewReviewIdOrderByCreatedAtAsc(reviewId).size();

        ReviewResponseDto responseDto = new ReviewResponseDto(review, member.getNickname(), commentCount);

        // 조회수 설정
        viewCountRepository.findById(reviewId)
                .ifPresent(vc -> responseDto.setViewCount(vc.getCount()));

        return responseDto;
    }

    // 리뷰 삭제
    @Transactional
    public void deleteReview(Long reviewId, Member member) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ServiceException("404-1", "존재하지 않는 리뷰입니다."));

        // 작성자 본인인지 확인
        if (!review.getMember().getId().equals(member.getId())) {
            throw new ServiceException("403-1", "리뷰 작성자만 삭제할 수 있습니다.");
        }

        // 조회수 데이터 먼저 삭제
        viewCountRepository.deleteById(reviewId);

        // 리뷰 삭제
        reviewRepository.delete(review);
    }

    // 인기 게시물 조회
    public List<ReviewResponseDto> getPopularReviews(int limit) {
        List<Review> allReviews = reviewRepository.findAll();

        // 리뷰별 점수 계산(조회수 + 평점 + 댓글수) 후 정렬
        List<ReviewWithScore> reviewsWithScores = allReviews.stream()
                .map(review -> {
                    int viewCount = viewCountRepository.findById(review.getReviewId())
                            .map(ReviewViewCount::getCount)
                            .orElse(0);
                    int commentCount = commentRepository.findByReviewReviewIdOrderByCreatedAtAsc(review.getReviewId()).size();
                    // 가중치 점수 계산 예시
                    double score = (viewCount * 0.5) + (review.getRating() * 2.0) + (commentCount * 1.5);
                    return new ReviewWithScore(review, score, commentCount, viewCount);
                })
                .sorted((r1, r2) -> Double.compare(r2.score, r1.score)) // 내림차순
                .limit(limit)
                .collect(Collectors.toList());

        // DTO 변환
        return reviewsWithScores.stream()
                .map(rws -> {
                    ReviewResponseDto dto = new ReviewResponseDto(
                            rws.review,
                            rws.review.getMember().getNickname(),
                            rws.commentCount
                    );
                    dto.setViewCount(rws.viewCount);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // 리뷰 목록 조회 (정렬, 검색)
    public List<ReviewResponseDto> getReviews(String sort, String keyword, Long placeId, Long memberId) {
        List<Review> reviews = new ArrayList<>();

        List<String> validSortOptions = Arrays.asList(
                "newest", "oldest", "highest_rating", "lowest_rating", "comments", "most_viewed"
        );
        if (!validSortOptions.contains(sort)) {
            throw new ServiceException("400-4", "유효하지 않은 정렬 옵션입니다.");
        }

        // 특정 회원 리뷰
        if (memberId != null) {
            reviews = reviewRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
        }
        // 특정 여행지 리뷰
        else if (placeId != null) {
            switch (sort) {
                case "highest_rating":
                    reviews = reviewRepository.findByPlace_IdOrderByRatingDesc(placeId);
                    break;
                case "lowest_rating":
                    reviews = reviewRepository.findByPlace_IdOrderByRatingAsc(placeId);
                    break;
                default:
                    reviews = reviewRepository.findByPlace_IdOrderByCreatedAtDesc(placeId);
                    break;
            }
        }
        // 제목 검색
        else if (keyword != null && !keyword.trim().isEmpty()) {
            reviews = reviewRepository.findByTitleContainingOrderByCreatedAtDesc(keyword);
        }
        // 전체 조회
        else {
            switch (sort) {
                case "comments":
                    List<Object[]> commentCountResult = reviewRepository.findAllOrderByCommentCountDesc();
                    return processCommentSortedResults(commentCountResult);
                case "oldest":
                    reviews = reviewRepository.findAllByOrderByCreatedAtAsc();
                    break;
                case "highest_rating":
                    reviews = reviewRepository.findAllByOrderByRatingDesc();
                    break;
                case "lowest_rating":
                    reviews = reviewRepository.findAllByOrderByRatingAsc();
                    break;
                case "most_viewed":
                    return getMostViewedReviews();
                case "newest":
                default:
                    reviews = reviewRepository.findAllByOrderByCreatedAtDesc();
                    break;
            }
        }

        // DTO 변환
        List<ReviewResponseDto> reviewDtos = new ArrayList<>();
        for (Review review : reviews) {
            int commentCount = commentRepository.findByReviewReviewIdOrderByCreatedAtAsc(review.getReviewId()).size();
            ReviewResponseDto dto = new ReviewResponseDto(review, review.getMember().getNickname(), commentCount);

            viewCountRepository.findById(review.getReviewId())
                    .ifPresent(vc -> dto.setViewCount(vc.getCount()));
            reviewDtos.add(dto);
        }
        return reviewDtos;
    }

    // 조회수 기준 정렬
    private List<ReviewResponseDto> getMostViewedReviews() {
        List<ReviewViewCount> sortedViewCounts = viewCountRepository.findAll().stream()
                .sorted((vc1, vc2) -> Integer.compare(vc2.getCount(), vc1.getCount()))
                .collect(Collectors.toList());

        List<ReviewResponseDto> result = new ArrayList<>();
        for (ReviewViewCount vc : sortedViewCounts) {
            Review review = vc.getReview();
            int commentCount = commentRepository.findByReviewReviewIdOrderByCreatedAtAsc(review.getReviewId()).size();

            ReviewResponseDto dto = new ReviewResponseDto(review, review.getMember().getNickname(), commentCount);
            dto.setViewCount(vc.getCount());
            result.add(dto);
        }
        return result;
    }

    // 댓글 수 기준 정렬
    private List<ReviewResponseDto> processCommentSortedResults(List<Object[]> commentCountResult) {
        List<ReviewResponseDto> reviewDtos = new ArrayList<>();
        for (Object[] result : commentCountResult) {
            Review review = (Review) result[0];
            Long commentCount = (Long) result[1];

            ReviewResponseDto dto = new ReviewResponseDto(
                    review,
                    review.getMember().getNickname(),
                    commentCount.intValue()
            );

            viewCountRepository.findById(review.getReviewId())
                    .ifPresent(vc -> dto.setViewCount(vc.getCount()));
            reviewDtos.add(dto);
        }
        return reviewDtos;
    }

    // 내부 클래스 (인기 게시물 계산)
    private static class ReviewWithScore {
        private final Review review;
        private final double score;
        private final int commentCount;
        private final int viewCount;

        public ReviewWithScore(Review review, double score, int commentCount, int viewCount) {
            this.review = review;
            this.score = score;
            this.commentCount = commentCount;
            this.viewCount = viewCount;
        }
    }

    // 특정 회원 리뷰 조회
    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getReviewsByMember(Long memberId) {
        if (memberId == null) {
            throw new ServiceException("400-5", "회원 ID는 필수입니다.");
        }
        List<Review> reviews = reviewRepository.findByMemberIdOrderByCreatedAtDesc(memberId);

        return reviews.stream()
                .map(review -> {
                    int commentCount = commentRepository
                            .findByReviewReviewIdOrderByCreatedAtAsc(review.getReviewId()).size();
                    ReviewResponseDto dto = new ReviewResponseDto(review, review.getMember().getNickname(), commentCount);
                    viewCountRepository.findById(review.getReviewId())
                            .ifPresent(vc -> dto.setViewCount(vc.getCount()));
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
