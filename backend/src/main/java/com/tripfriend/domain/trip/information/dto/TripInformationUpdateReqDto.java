package com.tripfriend.domain.trip.information.dto;

import com.tripfriend.domain.trip.information.entity.Transportation;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripInformationUpdateReqDto {

    private Long tripInformationId; // 여행 정보 ID

    private Long placeId; // 장소 ID

    private LocalDateTime visitTime;

    private Integer duration;

    private Transportation transportation;

    private int cost;

    private String notes;
}
