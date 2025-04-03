package com.tripfriend.domain.review.dto;

import com.tripfriend.domain.review.entity.Review;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ReviewResponseDto {
    private Long reviewId;
    private String title;
    private String content;
    private double rating;
    private Long memberId;
    private String memberName;
    private String profileImage;
    private Long placeId;
    private String placeName;
    private int viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int commentCount;

    public ReviewResponseDto(Review review, String memberName, int commentCount) {
        this.reviewId = review.getReviewId();
        this.title = review.getTitle();
        this.content = review.getContent();
        this.rating = review.getRating();
        this.memberId = review.getMember().getId();
        this.memberName = memberName;
        this.profileImage = null;
        this.placeId = review.getPlace().getId();
        this.placeName = review.getPlace().getPlaceName();
        this.viewCount = 0;
        this.createdAt = review.getCreatedAt();
        this.updatedAt = review.getUpdatedAt();
        this.commentCount = commentCount;
    }

    public ReviewResponseDto(Review review, String memberName, int commentCount, String profileImage) {
        this(review, memberName, commentCount);
        this.profileImage = profileImage;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

}