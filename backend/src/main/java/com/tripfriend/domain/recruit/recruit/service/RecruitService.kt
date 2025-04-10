package com.tripfriend.domain.recruit.recruit.service

import com.tripfriend.domain.member.member.entity.AgeRange
import com.tripfriend.domain.member.member.entity.Gender
import com.tripfriend.domain.member.member.entity.Member
import com.tripfriend.domain.member.member.service.AuthService
import com.tripfriend.domain.place.place.repository.PlaceRepository
import com.tripfriend.domain.recruit.apply.dto.ApplyResponseDto
import com.tripfriend.domain.recruit.recruit.dto.RecruitDetailResponseDto
import com.tripfriend.domain.recruit.recruit.dto.RecruitListResponseDto
import com.tripfriend.domain.recruit.recruit.dto.RecruitRequestDto
import com.tripfriend.domain.recruit.recruit.repository.RecruitRepository
import com.tripfriend.global.exception.ServiceException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.*

@Service
class RecruitService(
    private val recruitRepository: RecruitRepository,
    private val placeRepository: PlaceRepository,
    private val authService: AuthService
) {

    fun getLoggedInMember(token: String?): Member {
        val member = authService.getLoggedInMember(token!!)
        return member ?: throw ServiceException("401-1", "로그인이 필요합니다.")
    }

    @Transactional
    fun findById(id: Long): RecruitDetailResponseDto {
        val recruit = recruitRepository.findById(id).orElseThrow {
            ServiceException("404-3", "해당 모집글이 존재하지 않습니다.")
        }
        val applies = recruit.applies.map { ApplyResponseDto(it) }
        return RecruitDetailResponseDto.fromWithApplies(recruit, applies)
    }

    @Transactional
    fun create(requestDto: RecruitRequestDto, token: String?): RecruitDetailResponseDto {
        val member = getLoggedInMember(token)
        val place = placeRepository.findById(requestDto.placeId!!)
            .orElseThrow { ServiceException("404-2", "해당 장소가 존재하지 않습니다.") }

        val recruit = requestDto.toEntity(member, place)
        return RecruitDetailResponseDto.from(recruitRepository.save(recruit))
    }

    @Transactional
    fun findAll(): List<RecruitListResponseDto> {
        return recruitRepository.findAllByOrderByCreatedAtDesc().map { RecruitListResponseDto(it) }
    }

    @Transactional
    fun findRecent3(): List<RecruitListResponseDto> {
        return recruitRepository.findTop3ByOrderByCreatedAtDesc().map { RecruitListResponseDto(it) }
    }

    @Transactional
    fun searchRecruits(keyword: String): List<RecruitListResponseDto> {
        return recruitRepository.searchByTitleOrContent(keyword).map { RecruitListResponseDto(it) }
    }

    @Transactional
    fun searchByIsClosed(isClosed: Boolean): List<RecruitListResponseDto> {
        return recruitRepository.findByIsClosed(isClosed).map { RecruitListResponseDto(it) }
    }

    @Transactional
    fun searchAndFilter(
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
        token: String?
    ): List<RecruitListResponseDto> {
        val member = token?.takeIf { it.isNotBlank() }?.let { getLoggedInMember(it) }

        val userGender = member?.gender
        val userAgeRange = member?.ageRange

        val adjustedSameGender = if (member != null) sameGender else null
        val adjustedSameAge = if (member != null) sameAge else null

        return recruitRepository.searchFilterSort(
            keyword, placeCityName, isClosed, startDate, endDate,
            travelStyle, adjustedSameGender, adjustedSameAge,
            minBudget, maxBudget, minGroupSize, maxGroupSize,
            sortBy, userGender, userAgeRange
        ).map { RecruitListResponseDto(it) }
    }

    @Transactional
    fun update(recruitId: Long, requestDto: RecruitRequestDto, token: String?): RecruitDetailResponseDto {
        val recruit = recruitRepository.findById(recruitId)
            .orElseThrow { ServiceException("404-3", "해당 모집글이 존재하지 않습니다.") }

        val place = placeRepository.findById(requestDto.placeId!!)
            .orElseThrow { ServiceException("404-2", "해당 장소가 존재하지 않습니다.") }

        val member = getLoggedInMember(token)

        if (recruit.member.id != member.id && member.authority != "ADMIN") {
            throw ServiceException("403-2", "관리자가 아니라면 본인이 등록한 동행 모집글만 수정할 수 있습니다.")
        }

        recruit.update(requestDto, place)
        return RecruitDetailResponseDto.from(recruit)
    }

    fun delete(recruitId: Long, token: String?) {
        val recruit = recruitRepository.findById(recruitId)
            .orElseThrow { ServiceException("404-3", "해당 모집글이 존재하지 않습니다.") }

        val member = getLoggedInMember(token)

        if (recruit.member.id != member.id && member.authority != "ADMIN") {
            throw ServiceException("403-2", "관리자가 아니라면 본인이 등록한 동행 모집글만 삭제할 수 있습니다.")
        }

        recruitRepository.deleteById(recruitId)
    }
}
