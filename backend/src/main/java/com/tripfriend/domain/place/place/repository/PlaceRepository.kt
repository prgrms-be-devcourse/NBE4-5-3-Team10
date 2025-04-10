package com.tripfriend.domain.place.place.repository

import com.tripfriend.domain.place.place.entity.Place
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface PlaceRepository : JpaRepository<Place, Long> {
    // 도시별로 장소 목록 조회
    fun findByCityName(cityName: String): List<Place>

    // 도시 목록 중복 제거
    @Query("SELECT DISTINCT p.cityName FROM Place p")
    fun findDistinctCityNames(): List<String>

    fun findByPlaceNameContainingIgnoreCaseAndCityNameContainingIgnoreCase(name: String, city: String): List<Place>

    fun findByPlaceNameContainingIgnoreCase(name: String): List<Place>

    fun findByCityNameContainingIgnoreCase(city: String): List<Place>
}
