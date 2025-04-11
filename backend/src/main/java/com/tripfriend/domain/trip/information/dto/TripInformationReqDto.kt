package com.tripfriend.domain.trip.information.dto

import com.tripfriend.domain.trip.information.entity.Transportation
import com.tripfriend.domain.trip.information.entity.TripInformation
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

data class TripInformationReqDto(
    var tripScheduleId: Long? = null,
    var tripInformationId: Long? = null,
    var placeId: @NotNull(message = "Place Id는 필수 입력값입니다.") Long? = null,
    var visitTime: LocalDateTime?,
    var duration: Int = 0,
    var transportation: Transportation?,
    var cost: Int = 0,
    var notes: String? = null,
) {
    constructor(info: TripInformation) : this(
        tripScheduleId = info.tripSchedule.id,
        tripInformationId = info.id,
        placeId = info.place.id,
        visitTime = info.visitTime,
        duration = info.duration,
        transportation = info.transportation,
        cost = info.cost,
        notes = info.notes,
    )
}
