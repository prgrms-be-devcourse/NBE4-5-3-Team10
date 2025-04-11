package com.tripfriend.domain.place.place.dto

import com.tripfriend.domain.place.place.entity.Category
import com.tripfriend.domain.place.place.entity.Place
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.springframework.web.multipart.MultipartFile

data class PlaceCreateReqDto(
    @field:NotBlank(message = "도시명을 입력해주세요.")
    var cityName: String,

    @field:NotBlank(message = "장소명을 입력해주세요.")
    var placeName: String,

    var description: String? = null,

    @field:NotNull(message = "카테고리를 선택해주세요")
    var category: Category,

    var imageUrl: MultipartFile? = null  // 단일 이미지
) {

    constructor(): this(
        cityName = "",
        placeName = "",
        description = "",
        category = Category.PLACE,
        imageUrl = null,
    )


    // DTO -> Entity 변환
    fun toEntity(): Place {
        return Place().apply {
            this.cityName = cityName
            this.placeName = placeName
            this.description = description
            this.category = category
        }
    }
}