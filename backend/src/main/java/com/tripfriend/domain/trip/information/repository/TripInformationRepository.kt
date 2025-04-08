package com.tripfriend.domain.trip.information.repository;

import com.tripfriend.domain.trip.information.entity.TripInformation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripInformationRepository extends JpaRepository<TripInformation, Long> {

    // 여행 일정 id에 따른 여행 정보 조회
    List<TripInformation> findByTripScheduleId(Long id);
}
