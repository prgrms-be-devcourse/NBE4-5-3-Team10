package com.tripfriend.domain.trip.schedule.repository;

import com.tripfriend.domain.trip.schedule.entity.TripSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripScheduleRepository extends JpaRepository<TripSchedule, Long> {

    // 특정 회원이 등록한 모든 여행 일정 조회
    List<TripSchedule> findByMemberId(Long memberId);
}
