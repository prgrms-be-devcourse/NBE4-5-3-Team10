package com.tripfriend.domain.trip.information.dto;

import com.tripfriend.domain.trip.information.entity.Transportation;
import com.tripfriend.domain.trip.information.entity.TripInformation;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripInformationResDto {
    private Long tripInformationId;
    private Long placeId;
    private String cityName;
    private String placeName;
    private LocalDateTime visitTime;
    private int duration;
    private Transportation transportation;
    private int cost;
    private String notes;
    //private int priority;
    private boolean isVisited;

    public TripInformationResDto(TripInformation tripInformation) {
        this.tripInformationId = tripInformation.getId();
        this.placeId = tripInformation.getPlace().getId();
        this.cityName = tripInformation.getPlace().getCityName();
        this.placeName = tripInformation.getPlace().getPlaceName();
        this.visitTime = tripInformation.getVisitTime();
        this.duration = tripInformation.getDuration();
        this.transportation = tripInformation.getTransportation();
        this.cost = tripInformation.getCost();
        this.notes = tripInformation.getNotes();
        //this.priority = tripInformation.getPriority();
        this.isVisited = tripInformation.isVisited();
    }
}
