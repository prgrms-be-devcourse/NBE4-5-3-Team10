package com.tripfriend.domain.trip.schedule.dto

import com.tripfriend.domain.trip.schedule.entity.TripSchedule


class TripScheduleResDto(tripSchedule: TripSchedule) {
    // 여행 일정 정보 DTO
    val id = tripSchedule.id
    val memberName = tripSchedule.member?.username
    val title = tripSchedule.title
    val cityName = tripSchedule.tripInformations[0].place?.cityName
    val description = tripSchedule.description
    val startDate = tripSchedule.startDate
    val endDate = tripSchedule.endDate
}
