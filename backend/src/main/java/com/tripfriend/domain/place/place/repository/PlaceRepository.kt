package com.tripfriend.domain.place.place.repository;

import com.tripfriend.domain.place.place.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {

    // 도시별로 장소 목록 조회
    List<Place> findByCityName(String cityName);

    // 도시 목록 중복 제거
    @Query("SELECT DISTINCT p.cityName FROM Place p")
    List<String> findDistinctCityNames();

    List<Place> findByPlaceNameContainingIgnoreCaseAndCityNameContainingIgnoreCase(String name, String city);

    List<Place> findByPlaceNameContainingIgnoreCase(String name);

    List<Place> findByCityNameContainingIgnoreCase(String city);
}
