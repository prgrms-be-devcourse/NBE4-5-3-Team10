package com.tripfriend.domain.trip.schedule.dto

import com.tripfriend.domain.trip.information.dto.TripInformationReqDto
import com.tripfriend.domain.trip.schedule.entity.TripSchedule
import java.time.LocalDate

data class TripScheduleReqDto(
    val memberId: Long?,
    val title: String?,
    val cityName: String? = null,
    val description: String?,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val tripInformations: MutableList<TripInformationReqDto> = mutableListOf()
) {
    constructor(trip: TripSchedule) : this(
        memberId = trip.member?.id,
        title = trip.title,
        cityName = trip.tripInformations[0].place?.cityName,
        description = trip.description,
        startDate = trip.startDate,
        endDate = trip.endDate,
        tripInformations = trip.tripInformations.map { TripInformationReqDto() }.toMutableList()
    )
}
