package com.tripfriend.domain.trip.information.dto;

import com.tripfriend.domain.trip.information.entity.Transportation;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripInformationReqDto {
    private Long tripScheduleId;
    private Long tripInformationId;
    @NotNull(message = "Place Id는 필수 입력값입니다.")
    private Long placeId;
    private LocalDateTime visitTime;
    private int duration;
    private Transportation transportation;
    private int cost;
    private String notes;
    //private int priority;
}
