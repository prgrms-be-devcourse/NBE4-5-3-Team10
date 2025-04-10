package com.tripfriend.domain.review.service

import com.tripfriend.domain.member.member.entity.Member
import com.tripfriend.domain.place.place.repository.PlaceRepository
import com.tripfriend.domain.review.dto.ReviewRequestDto
import com.tripfriend.domain.review.dto.ReviewResponseDto
import com.tripfriend.domain.review.entity.Review
import com.tripfriend.domain.review.entity.ReviewViewCount
import com.tripfriend.domain.review.repository.CommentRepository
import com.tripfriend.domain.review.repository.ReviewRepository
import com.tripfriend.domain.review.repository.ReviewViewCountRepository
import com.tripfriend.global.exception.ServiceException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ReviewService(
    private val reviewRepository: ReviewRepository,
    private val commentRepository: CommentRepository,
    private val viewCountRepository: ReviewViewCountRepository,
    private val placeRepository: PlaceRepository
) {

    // 리뷰 생성
    @Transactional
    fun createReview(requestDto: ReviewRequestDto, member: Member): ReviewResponseDto {
        // 유효성 검증
        if (requestDto.placeId == null) {
            throw ServiceException("400-1", "여행지 정보는 필수입니다.")
        }
        if (requestDto.rating < 1 || requestDto.rating > 5) {
            throw ServiceException("400-2", "평점은 1점에서 5점 사이여야 합니다.")
        }

        // placeId로 실제 Place 엔티티를 조회
        val place = placeRepository.findById(requestDto.placeId!!)
            .orElseThrow { ServiceException("404-2", "해당 여행지가 존재하지 않습니다.") }

        // Review 생성
        val review = Review(
            requestDto.title,
            requestDto.content,
            requestDto.rating,
            member,
            place
        )

        // 리뷰 저장
        val savedReview = reviewRepository.save(review)

        // 조회수 엔티티 초기화
        val viewCount = ReviewViewCount(savedReview)
        viewCountRepository.save(viewCount)

        // 댓글 수는 0이므로 그대로 DTO 반환
        return ReviewResponseDto(savedReview, member.nickname, 0)
    }

    // 리뷰 상세 조회
    @Transactional
    fun getReview(reviewId: Long, incrementView: Boolean): ReviewResponseDto {
        val review = reviewRepository.findById(reviewId)
            .orElseThrow { ServiceException("404-1", "존재하지 않는 리뷰입니다.") }

        // 조회수 처리
        val viewCount = viewCountRepository.findById(reviewId)
            .orElseGet {
                val newViewCount = ReviewViewCount(review)
                viewCountRepository.save(newViewCount)
            }

        // 새로운 조회일 경우에만 조회수 증가
        if (incrementView) {
            viewCount.increment()
            viewCountRepository.save(viewCount)
        }

        // 댓글 수 조회
        val commentCount = commentRepository.findByReviewReviewIdOrderByCreatedAtAsc(reviewId).size

        // 응답 DTO 생성
        val responseDto = ReviewResponseDto(
            review,
            review.member?.nickname ?: "",
            commentCount,
            review.member?.profileImage
        )
        responseDto.viewCount = viewCount.count

        return responseDto
    }

    // 원본 메서드(오버로딩)
    @Transactional
    fun getReview(reviewId: Long): ReviewResponseDto {
        return getReview(reviewId, true)
    }

    // 특정 장소의 리뷰 목록 조회
    fun getReviewsByPlace(placeId: Long?): List<ReviewResponseDto> {
        if (placeId == null) {
            throw ServiceException("400-3", "여행지 ID는 필수입니다.")
        }

        // placeId를 기준으로 검색
        val reviews = reviewRepository.findByPlace_IdOrderByCreatedAtDesc(placeId)

        return reviews.map { review ->
            val commentCount = commentRepository.findByReviewReviewIdOrderByCreatedAtAsc(review.reviewId!!).size
            val dto = ReviewResponseDto(review, review.member?.nickname ?: "", commentCount)

            // 조회수 설정
            viewCountRepository.findById(review.reviewId!!)
                .ifPresent { vc -> dto.viewCount = vc.count }

            dto
        }
    }

    // 리뷰 수정
    @Transactional
    fun updateReview(reviewId: Long, requestDto: ReviewRequestDto, member: Member): ReviewResponseDto {
        val review = reviewRepository.findById(reviewId)
            .orElseThrow { ServiceException("404-1", "존재하지 않는 리뷰입니다.") }

        // 작성자 본인인지 확인
        if (review.member?.id != member.id) {
            throw ServiceException("403-1", "리뷰 작성자만 수정할 수 있습니다.")
        }

        // 평점 유효성 검증
        if (requestDto.rating < 1 || requestDto.rating > 5) {
            throw ServiceException("400-2", "평점은 1점에서 5점 사이여야 합니다.")
        }

        // 내용 업데이트
        review.update(requestDto.title, requestDto.content, requestDto.rating)

        // 댓글 수
        val commentCount = commentRepository.findByReviewReviewIdOrderByCreatedAtAsc(reviewId).size

        val responseDto = ReviewResponseDto(review, member.nickname, commentCount)

        // 조회수 설정
        viewCountRepository.findById(reviewId)
            .ifPresent { vc -> responseDto.viewCount = vc.count }

        return responseDto
    }

    // 리뷰 삭제
    @Transactional
    fun deleteReview(reviewId: Long, member: Member) {
        val review = reviewRepository.findById(reviewId)
            .orElseThrow { ServiceException("404-1", "존재하지 않는 리뷰입니다.") }

        // 작성자 본인인지 확인
        if (review.member?.id != member.id) {
            throw ServiceException("403-1", "리뷰 작성자만 삭제할 수 있습니다.")
        }

        // 조회수 데이터 먼저 삭제
        viewCountRepository.deleteById(reviewId)

        // 리뷰 삭제
        reviewRepository.delete(review)
    }

    // 인기 게시물 조회
    fun getPopularReviews(limit: Int): List<ReviewResponseDto> {
        val allReviews = reviewRepository.findAll()

        // 리뷰별 점수 계산(조회수 + 평점 + 댓글수) 후 정렬
        val reviewsWithScores = allReviews.map { review ->
            val viewCount = viewCountRepository.findById(review.reviewId!!)
                .map { it.count }
                .orElse(0)
            val commentCount = commentRepository.findByReviewReviewIdOrderByCreatedAtAsc(review.reviewId!!).size
            // 가중치 점수 계산 예시
            val score = (viewCount * 0.5) + (review.rating * 2.0) + (commentCount * 1.5)
            ReviewWithScore(review, score, commentCount, viewCount)
        }
            .sortedByDescending { it.score } // 내림차순
            .take(limit)

        // DTO 변환
        return reviewsWithScores.map { rws ->
            val dto = ReviewResponseDto(
                rws.review,
                rws.review.member?.nickname ?: "",
                rws.commentCount
            )
            dto.viewCount = rws.viewCount
            dto
        }
    }

    // 리뷰 목록 조회 (정렬, 검색)
    fun getReviews(sort: String, keyword: String?, placeId: Long?, memberId: Long?): List<ReviewResponseDto> {
        val reviews = mutableListOf<Review>()

        val validSortOptions = listOf(
            "newest", "oldest", "highest_rating", "lowest_rating", "comments", "most_viewed"
        )
        if (!validSortOptions.contains(sort)) {
            throw ServiceException("400-4", "유효하지 않은 정렬 옵션입니다.")
        }

        // 특정 회원 리뷰
        when {
            memberId != null -> {
                reviews.addAll(reviewRepository.findByMemberIdOrderByCreatedAtDesc(memberId))
            }
            // 특정 여행지 리뷰
            placeId != null -> {
                when (sort) {
                    "newest" -> reviews.addAll(reviewRepository.findByPlace_IdOrderByCreatedAtDesc(placeId))
                    "oldest" -> reviews.addAll(reviewRepository.findByPlace_IdOrderByCreatedAtAsc(placeId))
                    "highest_rating" -> reviews.addAll(reviewRepository.findByPlace_IdOrderByRatingDesc(placeId))
                    "lowest_rating" -> reviews.addAll(reviewRepository.findByPlace_IdOrderByRatingAsc(placeId))
                    "comments" -> reviews.addAll(reviewRepository.findByPlace_IdOrderByCommentCountDesc(placeId))
                    "most_viewed" -> return getMostViewedReviewsByPlace(placeId)
                    else -> reviews.addAll(reviewRepository.findByPlace_IdOrderByCreatedAtDesc(placeId))
                }
            }
            // 제목 검색
            !keyword.isNullOrBlank() -> {
                reviews.addAll(reviewRepository.findByTitleContainingOrderByCreatedAtDesc(keyword))
            }
            // 전체 조회
            else -> {
                when (sort) {
                    "comments" -> {
                        return processCommentSortedResults(reviewRepository.findAllOrderByCommentCountDesc())
                    }
                    "oldest" -> reviews.addAll(reviewRepository.findAllByOrderByCreatedAtAsc())
                    "highest_rating" -> reviews.addAll(reviewRepository.findAllByOrderByRatingDesc())
                    "lowest_rating" -> reviews.addAll(reviewRepository.findAllByOrderByRatingAsc())
                    "most_viewed" -> return getMostViewedReviews()
                    else -> reviews.addAll(reviewRepository.findAllByOrderByCreatedAtDesc())
                }
            }
        }

        // DTO 변환
        return reviews.map { review ->
            val commentCount = commentRepository.findByReviewReviewIdOrderByCreatedAtAsc(review.reviewId!!).size
            val dto = ReviewResponseDto(review, review.member?.nickname ?: "", commentCount)

            viewCountRepository.findById(review.reviewId!!)
                .ifPresent { vc -> dto.viewCount = vc.count }
            dto
        }
    }

    // 조회수 기준 정렬
    private fun getMostViewedReviews(): List<ReviewResponseDto> {
        val sortedViewCounts = viewCountRepository.findAll()
            .sortedByDescending { it.count }

        return sortedViewCounts.map { vc ->
            val review = vc.review!!
            val commentCount = commentRepository.findByReviewReviewIdOrderByCreatedAtAsc(review.reviewId!!).size

            val dto = ReviewResponseDto(review, review.member?.nickname ?: "", commentCount)
            dto.viewCount = vc.count
            dto
        }
    }

    // 조회수 기준 정렬 - 특정 여행지
    private fun getMostViewedReviewsByPlace(placeId: Long): List<ReviewResponseDto> {
        val reviewsForPlace = reviewRepository.findByPlace_IdOrderByCreatedAtDesc(placeId)
        val sortedReviews = reviewsForPlace.sortedByDescending { review ->
            viewCountRepository.findById(review.reviewId!!).orElse(null)?.count ?: 0
        }
        return sortedReviews.map { review ->
            val commentCount = commentRepository.findByReviewReviewIdOrderByCreatedAtAsc(review.reviewId!!).size
            val dto = ReviewResponseDto(review, review.member?.nickname ?: "", commentCount)
            viewCountRepository.findById(review.reviewId!!)
                .ifPresent { vc -> dto.viewCount = vc.count }
            dto
        }
    }

    // 댓글 수 기준 정렬 (전체)
    private fun processCommentSortedResults(commentCountResult: List<Array<Any>>): List<ReviewResponseDto> {
        return commentCountResult.map { result ->
            val review = result[0] as Review
            val commentCount = (result[1] as Long).toInt()
            val dto = ReviewResponseDto(review, review.member?.nickname ?: "", commentCount)
            viewCountRepository.findById(review.reviewId!!)
                .ifPresent { vc -> dto.viewCount = vc.count }
            dto
        }
    }

    // 특정 회원 리뷰 조회
    @Transactional(readOnly = true)
    fun getReviewsByMember(memberId: Long?): List<ReviewResponseDto> {
        if (memberId == null) {
            throw ServiceException("400-5", "회원 ID는 필수입니다.")
        }
        val reviews = reviewRepository.findByMemberIdOrderByCreatedAtDesc(memberId)

        return reviews.map { review ->
            val commentCount = commentRepository
                .findByReviewReviewIdOrderByCreatedAtAsc(review.reviewId!!).size
            val dto = ReviewResponseDto(review, review.member?.nickname ?: "", commentCount)
            viewCountRepository.findById(review.reviewId!!)
                .ifPresent { vc -> dto.viewCount = vc.count }
            dto
        }
    }

    // 내부 데이터 클래스 (인기 게시물 계산)
    private data class ReviewWithScore(
        val review: Review,
        val score: Double,
        val commentCount: Int,
        val viewCount: Int
    )
}