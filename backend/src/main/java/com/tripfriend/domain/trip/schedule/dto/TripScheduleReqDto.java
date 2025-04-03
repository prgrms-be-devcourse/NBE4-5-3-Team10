package com.tripfriend.domain.trip.schedule.dto;

import com.tripfriend.domain.trip.information.dto.TripInformationReqDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TripScheduleReqDto {
    private Long memberId;
    private String title;
    private String cityName;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<TripInformationReqDto> tripInformations;
}
