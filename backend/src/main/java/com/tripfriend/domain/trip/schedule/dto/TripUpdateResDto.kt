package com.tripfriend.domain.trip.schedule.dto

import com.tripfriend.domain.trip.information.dto.TripInformationResDto
import com.tripfriend.domain.trip.information.entity.TripInformation
import com.tripfriend.domain.trip.schedule.entity.TripSchedule


class TripUpdateResDto(tripSchedule: TripSchedule, tripInformations: List<TripInformation?>) {
    // 수정된 여행 일정 정보를 담는 DTO
    val updatedSchedule = TripScheduleResDto(tripSchedule) // 일정 정보를 DTO로 변환

    // 수정된 여행 정보 리스트를 담는 DTO
    val updatedTripInformations: List<TripInformationResDto> = tripInformations.stream()
        .map { tripInformation: TripInformation? -> TripInformationResDto(tripInformation) }  // 여행 정보 리스트를 DTO로 변환
        .toList()
}
