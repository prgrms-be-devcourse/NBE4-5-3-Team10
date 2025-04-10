package com.tripfriend.domain.recruit.recruit.repository

import com.tripfriend.domain.member.member.entity.AgeRange
import com.tripfriend.domain.member.member.entity.Gender
import com.tripfriend.domain.recruit.recruit.entity.Recruit
import java.time.LocalDate

interface RecruitRepositoryCustom {
    fun findByRecruitTest(): List<Recruit>
    fun searchByTitleOrContent(keyword: String): List<Recruit>
    fun findByIsClosed(isClosed: Boolean): List<Recruit>
    fun searchFilterSort(
        keyword: String?,
        placeCityName: String?,
        isClosed: Boolean?,
        startDate: LocalDate?,
        endDate: LocalDate?,
        travelStyle: String?,
        sameGender: Boolean?,
        sameAge: Boolean?,
        minBudget: Int?,
        maxBudget: Int?,
        minGroupSize: Int?,
        maxGroupSize: Int?,
        sortBy: String?,
        userGender: Gender?,
        userAgeRange: AgeRange?
    ): List<Recruit>
}

