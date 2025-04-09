package com.tripfriend.domain.trip.information.dto

import com.tripfriend.domain.trip.information.entity.TripInformation


class TripInformationResDto(tripInformation: TripInformation) {
    var tripInformationId = tripInformation.id
    var placeId = tripInformation.place?.id
    var cityName = tripInformation.place?.cityName
    var placeName = tripInformation.place?.placeName
    var visitTime = tripInformation.visitTime
    var duration = tripInformation.duration
    var transportation = tripInformation.transportation
    var cost = tripInformation.cost
    var notes = tripInformation.notes
    var isVisited = tripInformation.isVisited
}
