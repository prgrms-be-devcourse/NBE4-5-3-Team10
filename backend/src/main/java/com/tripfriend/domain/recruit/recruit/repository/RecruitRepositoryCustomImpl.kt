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
import java.util.*

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
    ): List<Recruit> {
        val builder = BooleanBuilder()

        keyword.ifPresent { k ->
            builder.and(
                recruit.title.containsIgnoreCase(k)
                    .or(recruit.content.containsIgnoreCase(k))
            )
        }

        placeCityName.ifPresent { city -> builder.and(recruit.place.cityName.eq(city)) }
        isClosed.ifPresent { c -> builder.and(recruit.isClosed.eq(c)) }
        startDate.ifPresent { start -> builder.and(recruit.startDate.goe(start)) }
        endDate.ifPresent { end -> builder.and(recruit.endDate.loe(end)) }
        travelStyle.ifPresent { style -> builder.and(recruit.travelStyle.stringValue().eq(style)) }

        sameGender.ifPresent { sg ->
            if (sg && userGender != null) {
                builder.and(
                    recruit.member.gender.eq(userGender)
                        .or(recruit.sameGender.isFalse)
                )
            }
        }

        sameAge.ifPresent { sa ->
            if (sa && userAgeRange != null) {
                builder.and(
                    recruit.member.ageRange.eq(userAgeRange)
                        .or(recruit.sameAge.isFalse)
                )
            }
        }

        minBudget.ifPresent { builder.and(recruit.budget.goe(it)) }
        maxBudget.ifPresent { builder.and(recruit.budget.loe(it)) }
        minGroupSize.ifPresent { builder.and(recruit.groupSize.goe(it)) }
        maxGroupSize.ifPresent { builder.and(recruit.groupSize.loe(it)) }

        val orderSpecifier = getOrderSpecifier(sortBy)

        return jpaQueryFactory.selectFrom(recruit)
            .where(builder)
            .orderBy(orderSpecifier)
            .fetch()
    }

    private fun getOrderSpecifier(sortBy: Optional<String>): OrderSpecifier<*> {
        return when (sortBy.orElse("created_desc").lowercase()) {
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
