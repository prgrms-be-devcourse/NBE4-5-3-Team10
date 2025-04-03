package com.tripfriend.domain.trip.schedule.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tripfriend.domain.member.member.entity.Member;
import com.tripfriend.domain.trip.information.entity.TripInformation;
import com.tripfriend.domain.trip.schedule.dto.TripScheduleUpdateReqDto;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "trip_schedule")
public class TripSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trip_schedule_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    // 여행지 연결 테이블 리스트
    @OneToMany(mappedBy = "tripSchedule", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<TripInformation> tripInformations = new ArrayList<>();

    @Column(name = "title", nullable = false)
    private String title; // 제목

    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // 일정 설명

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate; // 시작일

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate; // 종료일

    @CreatedDate
    @Setter(AccessLevel.PRIVATE)
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // 생성일

    @LastModifiedDate
    @Setter(AccessLevel.PRIVATE)
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 수정일

    // 단일 TripInformation을 여행 일정에 추가
    public void addTripInfromation(TripInformation tripInformation){
        this.tripInformations.add(tripInformation);
        tripInformation.setTripSchedule(this); // 연관 관계 설정
    }

    // 여행 일정에 여러 개의 TripInformation을 추가
    public void addTripInformations(List<TripInformation> tripInformations) {
        if(tripInformations != null){
            for(TripInformation tripInformation : tripInformations){
                addTripInfromation(tripInformation);
            }
        }
    }

    // 여행 일정 정보 수정 메서드
    public void updateSchedule(TripScheduleUpdateReqDto scheduleUpdate) {
        this.title = scheduleUpdate.getTitle();
        this.description = scheduleUpdate.getDescription();
        this.startDate = scheduleUpdate.getStartDate();
        this.endDate = scheduleUpdate.getEndDate();
        this.updatedAt = LocalDateTime.now(); // 수정 시간 업데이트
    }
}

