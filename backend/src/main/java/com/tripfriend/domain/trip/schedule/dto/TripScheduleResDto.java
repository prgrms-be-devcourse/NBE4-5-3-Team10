package com.tripfriend.domain.trip.schedule.dto;

import com.tripfriend.domain.place.place.dto.PlaceResDto;
import com.tripfriend.domain.trip.information.dto.TripInformationReqDto;
import com.tripfriend.domain.trip.information.dto.TripInformationResDto;
import com.tripfriend.domain.trip.information.entity.TripInformation;
import com.tripfriend.domain.trip.schedule.entity.TripSchedule;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class TripScheduleResDto { // 여행 일정 정보 DTO
    private Long id;
    private String memberName;
    private String title;
    private String cityName;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
//    private List<TripInformationResDto> tripInformations; // 해당 여행지 일정에 대한 정보

    // TripSchedule 엔티티를 DTO로 변환하는 생성자
    public TripScheduleResDto(TripSchedule tripSchedule) {
        this.id = tripSchedule.getId();
        this.memberName = tripSchedule.getMember().getUsername();
        this.title = tripSchedule.getTitle();
        this.cityName = tripSchedule.getTripInformations().get(0).getPlace().getCityName();
        this.description = tripSchedule.getDescription();
        this.startDate = tripSchedule.getStartDate();
        this.endDate = tripSchedule.getEndDate();

//        this.tripInformations = tripSchedule.getTripInformations()
//                .stream()
//                .map(TripInformationResDto::new)
//                .collect(Collectors.toList());
    }
}
