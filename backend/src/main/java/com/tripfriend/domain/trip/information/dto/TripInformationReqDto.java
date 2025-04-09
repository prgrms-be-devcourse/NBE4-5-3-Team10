package com.tripfriend.domain.trip.information.dto;

import com.tripfriend.domain.trip.information.entity.Transportation;
import com.tripfriend.domain.trip.information.entity.TripInformation;
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

    public TripInformationReqDto(TripInformation info) {
        this.tripScheduleId = info.getTripSchedule() != null ? info.getTripSchedule().getId() : null;
        this.tripInformationId = info.getId();
        this.placeId = info.getPlace() != null ? info.getPlace().getId() : null;
        this.visitTime = info.getVisitTime();
        this.duration = info.getDuration();
        this.transportation = info.getTransportation();
        this.cost = info.getCost();
        this.notes = info.getNotes();
    }
}
