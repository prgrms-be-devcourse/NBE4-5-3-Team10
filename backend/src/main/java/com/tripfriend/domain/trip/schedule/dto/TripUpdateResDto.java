package com.tripfriend.domain.trip.schedule.dto;

import com.tripfriend.domain.trip.information.dto.TripInformationResDto;
import com.tripfriend.domain.trip.information.entity.TripInformation;
import com.tripfriend.domain.trip.schedule.entity.TripSchedule;
import lombok.Getter;

import java.util.List;

@Getter
public class TripUpdateResDto {

    // 수정된 여행 일정 정보를 담는 DTO
    private final TripScheduleResDto updatedSchedule;

    // 수정된 여행 정보 리스트를 담는 DTO
    private final List<TripInformationResDto> updatedTripInformations;

    /**
     * TripUpdateResDto 생성자
     *
     * @param tripSchedule       수정된 여행 일정 엔티티
     * @param tripInformations   수정된 여행 정보 리스트 엔티티
     */
    public TripUpdateResDto(TripSchedule tripSchedule, List<TripInformation> tripInformations) {
        this.updatedSchedule = new TripScheduleResDto(tripSchedule); // 일정 정보를 DTO로 변환
        this.updatedTripInformations = tripInformations.stream()
                .map(TripInformationResDto::new) // 여행 정보 리스트를 DTO로 변환
                .toList();
    }
}
