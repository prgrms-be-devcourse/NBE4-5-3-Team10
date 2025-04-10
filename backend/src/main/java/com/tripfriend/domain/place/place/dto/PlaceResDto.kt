package com.tripfriend.domain.place.place.dto;

import com.tripfriend.domain.place.place.entity.Category;
import com.tripfriend.domain.place.place.entity.Place;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PlaceResDto {
    private Long id;
    private String cityName;
    private String placeName;
    private String description;
    private Category category;
    private String imageUrl;


    public PlaceResDto(Place place) {
        this.id = place.getId();
        this.cityName = place.getCityName();
        this.placeName = place.getPlaceName();
        this.description = place.getDescription();
        this.category = place.getCategory();
        this.imageUrl = place.getImageUrl();
    }
}
