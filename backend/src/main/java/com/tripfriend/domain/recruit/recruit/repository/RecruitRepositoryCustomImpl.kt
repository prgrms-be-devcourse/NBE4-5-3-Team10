package com.tripfriend.domain.recruit.recruit.repository

import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.impl.JPAQueryFactory
import com.tripfriend.domain.member.member.entity.AgeRange
import com.tripfriend.domain.member.member.entity.Gender
import com.tripfriend.domain.recruit.recruit.entity.QRecruit
import com.tripfriend.domain.recruit.recruit.entity.Recruit
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class RecruitRepositoryCustomImpl(
    private val jpaQueryFactory: JPAQueryFactory
) : RecruitRepositoryCustom {

    private val recruit = QRecruit.recruit

    override fun findByRecruitTest(): List<Recruit> {
        return jpaQueryFactory.selectFrom(recruit)
            .where(recruit.recruitId.eq(1L))
            .fetch()
    }

    override fun searchByTitleOrContent(keyword: String): List<Recruit> {
        return jpaQueryFactory.selectFrom(recruit)
            .where(
                recruit.title.containsIgnoreCase(keyword)
                    .or(recruit.content.containsIgnoreCase(keyword))
            )
            .fetch()
    }

    override fun findByIsClosed(isClosed: Boolean): List<Recruit> {
        return jpaQueryFactory.selectFrom(recruit)
            .where(recruit.isClosed.eq(isClosed))
            .fetch()
    }

    override fun searchFilterSort(
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
    ): List<Recruit> {
        val builder = BooleanBuilder()

        keyword?.let {
            builder.and(
                recruit.title.containsIgnoreCase(it)
                    .or(recruit.content.containsIgnoreCase(it))
            )
        }

        placeCityName?.let { builder.and(recruit.place.cityName.eq(it)) }
        isClosed?.let { builder.and(recruit.isClosed.eq(it)) }
        startDate?.let { builder.and(recruit.startDate.goe(it)) }
        endDate?.let { builder.and(recruit.endDate.loe(it)) }
        travelStyle?.let { builder.and(recruit.travelStyle.stringValue().eq(it)) }

        if(sameGender == true && userGender != null){
            builder.and(
                recruit.member.gender.eq(userGender)
                    .or(recruit.sameGender.isFalse)
            )
        }

        if(sameAge == true && userAgeRange != null){
            builder.and(
                recruit.member.ageRange.eq(userAgeRange)
                    .or(recruit.sameAge.isFalse)
            )
        }

        minBudget?.let { builder.and(recruit.budget.goe(it)) }
        maxBudget?.let { builder.and(recruit.budget.loe(it)) }
        minGroupSize?.let { builder.and(recruit.groupSize.goe(it)) }
        maxGroupSize?.let { builder.and(recruit.groupSize.loe(it)) }

        val orderSpecifier = getOrderSpecifier(sortBy)

        return jpaQueryFactory.selectFrom(recruit)
            .where(builder)
            .orderBy(orderSpecifier)
            .fetch()
    }

    private fun getOrderSpecifier(sortBy: String?): OrderSpecifier<*> {
        return when (sortBy?.lowercase() ?:"created_desc") {
            "startdate_asc" -> recruit.startDate.asc()
            "enddate_desc" -> recruit.endDate.desc()
            "trip_duration" -> Expressions.numberTemplate(
                Int::class.java,
                "TIMESTAMPDIFF(DAY, {0}, {1})",
                recruit.startDate,
                recruit.endDate
            ).desc()
            "budget_asc" -> recruit.budget.asc()
            "budget_desc" -> recruit.budget.desc()
            "groupsize_asc" -> recruit.groupSize.asc()
            "groupsize_desc" -> recruit.groupSize.desc()
            else -> recruit.createdAt.desc()
        }
    }
}
