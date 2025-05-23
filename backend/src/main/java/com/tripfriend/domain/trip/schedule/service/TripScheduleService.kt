package com.tripfriend.domain.trip.schedule.service

import com.tripfriend.domain.member.member.entity.Member
import com.tripfriend.domain.member.member.repository.MemberRepository
import com.tripfriend.domain.member.member.service.AuthService
import com.tripfriend.domain.place.place.repository.PlaceRepository
import com.tripfriend.domain.trip.information.dto.TripInformationReqDto
import com.tripfriend.domain.trip.information.repository.TripInformationRepository
import com.tripfriend.domain.trip.information.service.TripInformationService
import com.tripfriend.domain.trip.schedule.dto.*
import com.tripfriend.domain.trip.schedule.entity.TripSchedule
import com.tripfriend.domain.trip.schedule.repository.TripScheduleRepository
import com.tripfriend.global.exception.ServiceException
import jakarta.validation.Valid
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TripScheduleService(
    private val tripScheduleRepository: TripScheduleRepository,
    private val memberRepository: MemberRepository,
    private val tripInformationService: TripInformationService,
    private val tripInformationRepository: TripInformationRepository,
    private val authService: AuthService,
    private val placeRepository: PlaceRepository
) {

    /**
     * 현재 로그인한 회원객체를 반환하는 메서드
     *
     * @param token JWT 토큰
     * @return 로그인한 회원 객체
     * @throws ServiceException 로그인하지 않은 경우 예외 발생
     */
    fun getLoggedInMember(token: String): Member =
        authService.getLoggedInMember(token)
            ?: throw ServiceException("401-2", "로그인이 필요합니다.")

    // 여행 일정 생성
    @Transactional
    fun createSchedule(req: TripScheduleReqDto, token: String): TripScheduleInfoResDto {
        // 로그인한 회원 정보 및 도시 유효성 검증
        val member = getLoggedInMember(token)
        val selectedCity = validateCity(req.cityName ?: throw ServiceException("400-2", "여행지(도시명) 선택은 필수입니다."))

        // 여행 일정 생성 및 저장
        val newSchedule = createAndSaveSchedule(member, req)

        // 세부일정 검증 및 저장
        if (!req.tripInformations.isNullOrEmpty()) {
            validateTripInformations(req.tripInformations, selectedCity)
            tripInformationService.addTripInformations(newSchedule, req.tripInformations)
        }
        return TripScheduleInfoResDto(newSchedule)
    }

    // 도시명 유효성 검증
    private fun validateCity(cityName: String): String {
        if (cityName.isBlank()) {
            throw ServiceException("400-2", "여행지(도시명) 선택은 필수입니다.")
        }
        return cityName
    }

    // 여행 일정을 생성하고 DB에 저장
    private fun createAndSaveSchedule(member: Member, req: TripScheduleReqDto): TripSchedule {
        val schedule = TripSchedule().apply {
            this.member = member
            this.title = req.title
            this.description = req.description
            this.startDate = req.startDate
            this.endDate = req.endDate
        }
        tripScheduleRepository.save(schedule)
        return schedule
    }

    // 각 세부일정의 장소가 선택한 도시와 일치하는지 검증
    private fun validateTripInformations(tripInfos: List<TripInformationReqDto>, selectedCity: String) {
        tripInfos.forEach { tripInfo ->
            val place =
                placeRepository.findById(tripInfo.placeId ?: throw ServiceException("404-2", "해당 장소가 존재하지 않습니다."))
                    .orElseThrow { ServiceException("404-2", "해당 장소가 존재하지 않습니다.") }
            if (place.cityName != selectedCity) {
                throw ServiceException("400-1", "선택한 도시와 일치하지 않는 장소가 포함되어 있습니다.")
            }
        }
    }

    @get:Transactional(readOnly = true)
    val allSchedules: List<TripScheduleResDto>
        get() {
            val schedules = tripScheduleRepository.findAll()
            if (schedules.isEmpty()) {
                throw ServiceException("404-3", "여행 일정이 존재하지 않습니다.")
            }
            return schedules.map { TripScheduleResDto(it) }
        }

    // 회원 이름 조회
    @Transactional(readOnly = true)
    fun getMemberName(memberId: Long): String =
        memberRepository.findById(memberId)
            .map { it.username }
            .orElseThrow { ServiceException("404-1", "해당 회원이 존재하지 않습니다.") }

    // 특정 회원의 여행 일정 조회
    @Transactional(readOnly = true)
    fun getSchedulesByMemberId(memberId: Long): List<TripScheduleResDto> {
        if (!memberRepository.existsById(memberId)) {
            throw ServiceException("404-1", "해당 회원이 존재하지 않습니다.")
        }
        val schedules = tripScheduleRepository.findByMemberId(memberId)
        if (schedules.isEmpty()) {
            throw ServiceException("404-3", "해당 회원의 여행 일정이 존재하지 않습니다.")
        }
        return schedules.map { TripScheduleResDto(it) }
    }

    /**
     * 로그인한 회원이 자신의 여행 일정을 삭제하는 메서드
     *
     * @param scheduleId 삭제할 일정 ID
     * @param token      JWT 토큰 (로그인된 사용자 확인)
     * @throws ServiceException 로그인되지 않았거나, 권한이 없는 경우 예외 발생
     */
    @Transactional
    fun deleteSchedule(scheduleId: Long, token: String) {
        val member = getLoggedInMember(token)
        val schedule = tripScheduleRepository.findById(scheduleId)
            .orElseThrow { ServiceException("404-1", "일정이 존재하지 않습니다.") }

        if (schedule.member?.id != member.id) {
            throw ServiceException("403-1", "본인이 생성한 일정만 삭제할 수 있습니다.")
        }
        tripScheduleRepository.delete(schedule)
    }

    /**
     * 특정 여행 일정을 수정하는 메서드
     *
     * @param scheduleId 수정할 여행 일정의 ID
     * @param req        일정 수정 요청 DTO
     * @return 수정된 TripSchedule 객체
     */
    @Transactional
    fun updateSchedule(scheduleId: Long, req: TripScheduleUpdateReqDto): TripSchedule {
        val schedule = tripScheduleRepository.findById(scheduleId)
            .orElseThrow { ServiceException("404-1", "해당 일정이 존재하지 않습니다.") }
        schedule.updateSchedule(req)
        return schedule
    }

    // 여행 일정 및 여행 정보 통합 수정 메서드
    @Transactional
    fun updateTrip(reqDto: @Valid TripUpdateReqDto, token: String): TripUpdateResDto {
        val member = getLoggedInMember(token)
        val tripSchedule = tripScheduleRepository.findById(reqDto.tripScheduleId)
            .orElseThrow { ServiceException("404-1", "해당 일정이 존재하지 않습니다.") }

        if (tripSchedule.member?.id != member.id) {
            throw ServiceException("403-1", "본인이 생성한 일정만 수정할 수 있습니다.")
        }

        tripSchedule.updateSchedule(reqDto.scheduleUpdate)

        val updatedTripInformations = reqDto.tripInformationUpdates.map { infoUpdate ->
            val tripInfo = tripInformationRepository.findById(infoUpdate.tripInformationId!!)
                .orElseThrow { ServiceException("404-2", "해당 여행 정보가 존재하지 않습니다.") }
            if (tripInfo.tripSchedule.member?.id != member.id) {
                throw ServiceException("403-2", "본인이 생성한 일정의 여행 정보만 수정할 수 있습니다.")
            }
            tripInfo.updateTripInformation(infoUpdate)
            tripInfo
        }

        return TripUpdateResDto(tripSchedule, updatedTripInformations)
    }

    // 특정 회원이 생성한 여행 일정 조회
    @Transactional(readOnly = true)
    fun getSchedulesByCreator(token: String): List<TripScheduleResDto> {
        val member = getLoggedInMember(token)
        val schedules = tripScheduleRepository.findByMemberId(member.id!!)
        if (schedules.isEmpty()) {
            throw ServiceException("404-3", "해당 회원의 여행 일정이 존재하지 않습니다.")
        }
        return schedules.map { TripScheduleResDto(it) }
    }

    // 특정 회원이 생성한 여행 일정의 세부 정보 조회
    @Transactional
    fun getTripInfo(token: String, id: Long): List<TripScheduleInfoResDto> {
        val member = getLoggedInMember(token)
        val schedule = tripScheduleRepository.findById(id)
            .orElseThrow { ServiceException("404-1", "해당 일정이 존재하지 않습니다") }
        if (schedule.member?.id != member.id) {
            throw ServiceException("403-1", "본인이 생성한 일정만 조회할 수 있습니다.")
        }
        return listOf(TripScheduleInfoResDto(schedule))
    }
}