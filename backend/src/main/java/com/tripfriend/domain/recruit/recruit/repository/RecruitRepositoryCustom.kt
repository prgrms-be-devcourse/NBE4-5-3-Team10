package com.tripfriend.domain.recruit.recruit.repository

import com.tripfriend.domain.member.member.entity.AgeRange
import com.tripfriend.domain.member.member.entity.Gender
import com.tripfriend.domain.recruit.recruit.entity.Recruit
import java.time.LocalDate
import java.util.*

interface RecruitRepositoryCustom {
    fun findByRecruitTest(): List<Recruit>
    fun searchByTitleOrContent(keyword: String): List<Recruit>
    fun findByIsClosed(isClosed: Boolean): List<Recruit>
    fun searchFilterSort(
        keyword: Optional<String>,
        placeCityName: Optional<String>,
        isClosed: Optional<Boolean>,
        startDate: Optional<LocalDate>,
        endDate: Optional<LocalDate>,
        travelStyle: Optional<String>,
        sameGender: Optional<Boolean>,
        sameAge: Optional<Boolean>,
        minBudget: Optional<Int>,
        maxBudget: Optional<Int>,
        minGroupSize: Optional<Int>,
        maxGroupSize: Optional<Int>,
        sortBy: Optional<String>,
        userGender: Gender?,
        userAgeRange: AgeRange?
    ): List<Recruit>
}

