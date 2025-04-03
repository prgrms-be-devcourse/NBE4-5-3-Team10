package com.tripfriend.domain.place.place.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tripfriend.domain.recruit.recruit.entity.Recruit;
import com.tripfriend.domain.review.entity.Review;
import com.tripfriend.domain.trip.information.entity.TripInformation;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "place")
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "place_id")
    private Long id;

    // 여행 스케줄 연결 테이블 리스트
    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<TripInformation> tripInformations = new ArrayList<>();

    // 이미지 저장 URL
    @Column(name = "image_url")
    private String imageUrl;

    // 동행 게시글 1:N 연결
    @OneToMany(mappedBy = "place", cascade = CascadeType.REMOVE)
    @JsonIgnore
    private List<Recruit> recruits;

    // 후기 게시글 1:N 연결
    @OneToMany(mappedBy = "place", cascade = CascadeType.REMOVE)
    @JsonIgnore
    private List<Review> reviews;

    @Column(name = "city_name", nullable = false)
    private String cityName; // 도시명

    @Column(name = "place_name", nullable = false)
    private String placeName; // 장소명

    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // 설명

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private Category category; // 카테고리

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // 생성일

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 수정일

    public void addTripInformation(TripInformation tripInformation) {
        this.tripInformations.add(tripInformation);
        tripInformation.setPlace(this);  // 연관관계 설정
    }
}
