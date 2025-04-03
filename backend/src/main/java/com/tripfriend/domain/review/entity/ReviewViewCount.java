package com.tripfriend.domain.review.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "review_view_count")
public class ReviewViewCount {

    @Id
    private Long reviewId;  // Review의 ID를 그대로 PK로 사용

    @Column(nullable = false)
    private int count = 0;  // 조회수

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId  // reviewId를 PK로 사용
    @JoinColumn(name = "review_id")
    private Review review;

    public ReviewViewCount(Review review) {
        this.review = review;
        this.count = 0;
    }

    public void increment() {
        this.count++;
    }
}