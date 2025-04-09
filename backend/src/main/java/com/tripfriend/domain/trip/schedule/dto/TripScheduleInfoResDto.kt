package com.tripfriend.domain.trip.schedule.dto

import com.tripfriend.domain.trip.information.dto.TripInformationResDto
import com.tripfriend.domain.trip.information.entity.TripInformation
import com.tripfriend.domain.trip.schedule.entity.TripSchedule
import lombok.Getter
import java.util.stream.Collectors

class TripScheduleInfoResDto(tripSchedule: TripSchedule) {
    // 여행 일정 정보 DTO
    val id = tripSchedule.id
    val memberName = tripSchedule.member?.username
    val title = tripSchedule.title
    val cityName = tripSchedule.tripInformations[0].place?.cityName
    val description = tripSchedule.description
    val startDate = tripSchedule.startDate
    val endDate = tripSchedule.endDate

    val tripInformations: List<TripInformationResDto> = tripSchedule.tripInformations
        .stream()
        .map { tripInformation: TripInformation? -> TripInformationResDto(tripInformation) }
        .collect(Collectors.toList()) // 해당 여행지 일정에 대한 정보
}
