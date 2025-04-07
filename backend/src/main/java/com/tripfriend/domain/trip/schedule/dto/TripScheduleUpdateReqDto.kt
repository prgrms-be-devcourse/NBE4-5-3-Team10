package com.tripfriend.domain.trip.schedule.dto

import java.time.LocalDate

data class TripScheduleUpdateReqDto(
    var title: String,

    var description: String? = null,

    var startDate: LocalDate,

    var endDate: LocalDate,
)
