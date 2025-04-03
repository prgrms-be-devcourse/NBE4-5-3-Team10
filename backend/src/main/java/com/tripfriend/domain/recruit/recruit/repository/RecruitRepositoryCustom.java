package com.tripfriend.domain.recruit.recruit.repository;

import com.tripfriend.domain.member.member.entity.AgeRange;
import com.tripfriend.domain.member.member.entity.Gender;
import com.tripfriend.domain.recruit.recruit.entity.Recruit;
import com.tripfriend.domain.recruit.recruit.entity.TravelStyle;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RecruitRepositoryCustom {
    List<Recruit> findByRecruitTest();
    List<Recruit> searchByTitleOrContent(String keyword);
    List<Recruit> findByIsClosed(boolean isClosed);
    List<Recruit> searchFilterSort(
            Optional<String> keyword,
            Optional<String> placeCityName,
            Optional<Boolean> isClosed,
            Optional<LocalDate> startDate,
            Optional<LocalDate> endDate,
            Optional<String> travelStyle,
            Optional<Boolean> sameGender,
            Optional<Boolean> sameAge,
            Optional<Integer> minBudget,
            Optional<Integer> maxBudget,
            Optional<Integer> minGroupSize,
            Optional<Integer> maxGroupSize,
            Optional<String> sortBy,
            Gender userGender,
            AgeRange userAgeRange
    );
}
