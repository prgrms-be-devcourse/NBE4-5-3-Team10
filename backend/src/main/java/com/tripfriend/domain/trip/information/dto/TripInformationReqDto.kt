package com.tripfriend.domain.trip.information.dto

import com.tripfriend.domain.trip.information.entity.Transportation
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

data class TripInformationReqDto(
    var tripScheduleId: Long? = null,
    var tripInformationId: Long? = null,
    var placeId: @NotNull(message = "Place Id는 필수 입력값입니다.") Long? = null,
    var visitTime: LocalDateTime,
    var duration:Int = 0,
    var transportation: Transportation,
    var cost:Int = 0,
    var notes: String? = null,
)
