package com.tripfriend.domain.trip.information.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class VisitedReqDto(
    var tripInformationId: Long,

    @JsonProperty("isVisited")
    var isVisited: Boolean,
)
