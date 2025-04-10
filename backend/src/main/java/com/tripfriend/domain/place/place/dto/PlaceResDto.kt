package com.tripfriend.domain.place.place.dto

import com.tripfriend.domain.place.place.entity.Category
import com.tripfriend.domain.place.place.entity.Place

data class PlaceResDto(
    val id: Long,
    val cityName: String,
    val placeName: String,
    val description: String?,
    val category: Category,
    val imageUrl: String?,
) {
    constructor(place: Place) : this(
        id = place.id!!,
        cityName = place.cityName,
        placeName = place.placeName,
        description = place.description,
        category = place.category,
        imageUrl = place.imageUrl
    )
}