package com.tripfriend.domain.trip.schedule.dto

import com.tripfriend.domain.trip.information.dto.TripInformationUpdateReqDto
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull


data class TripUpdateReqDto(
    @field:NotNull
    val tripScheduleId: Long, // 여행 일정 ID

    @field:Valid
    val scheduleUpdate: TripScheduleUpdateReqDto, // 여행 일정 수정 정보

    @field:Valid
    val tripInformationUpdates: List<TripInformationUpdateReqDto>, // 여행 정보 수정 리스트
) {
}
