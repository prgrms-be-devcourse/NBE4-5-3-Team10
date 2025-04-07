package com.tripfriend.domain.trip.information.entity;

import com.tripfriend.domain.place.place.entity.Place;
import com.tripfriend.domain.trip.information.dto.TripInformationUpdateReqDto;
import com.tripfriend.domain.trip.schedule.entity.TripSchedule;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "trip_information")
public class TripInformation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trip_Information_id")
    private Long id; // 개별 Id 추가

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_schedule_id", nullable = false)
    private TripSchedule tripSchedule; // 여행일정Id - FK

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place; // 여행지Id - FK

    @Column(name = "visit_time", nullable = false)
    private LocalDateTime visitTime; // 방문시간

    @Column(name = "duration", nullable = false)
    private Integer duration; // 방문기간(날짜 단위)

    @Column(name = "transportation", nullable = false)
    private Transportation transportation; // 교통 수단

    @Column(name = "cost")
    private int cost; // 여행 경비

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes; // 메모

//    @Column(name = "priority")
//    private Integer priority; // 우선 순위

    @Column(name = "is_visited", nullable = false)
    @ColumnDefault("false")
    private boolean isVisited; // 방문여부

    public void setTripSchedule(TripSchedule tripSchedule) {
        this.tripSchedule = tripSchedule;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    public void setVisited(boolean isVisited){this.isVisited = isVisited;}

    // 여행 정보 수정 메서드
    public void updateTripInformation(TripInformationUpdateReqDto updateDto){
        this.visitTime = updateDto.getVisitTime();
        this.duration = updateDto.getDuration();
        this.transportation = updateDto.getTransportation();
        this.cost = updateDto.getCost();
        this.notes = updateDto.getNotes();
    }
}
