package com.tripfriend.domain.trip.schedule.service

import com.tripfriend.domain.member.member.entity.Member
import com.tripfriend.domain.member.member.repository.MemberRepository
import com.tripfriend.domain.member.member.service.AuthService
import com.tripfriend.domain.place.place.repository.PlaceRepository
import com.tripfriend.domain.trip.information.dto.TripInformationReqDto
import com.tripfriend.domain.trip.information.dto.TripInformationUpdateReqDto
import com.tripfriend.domain.trip.information.entity.TripInformation
import com.tripfriend.domain.trip.information.repository.TripInformationRepository
import com.tripfriend.domain.trip.information.service.TripInformationService
import com.tripfriend.domain.trip.schedule.dto.*
import com.tripfriend.domain.trip.schedule.entity.TripSchedule
import com.tripfriend.domain.trip.schedule.repository.TripScheduleRepository
import com.tripfriend.global.exception.ServiceException
import jakarta.validation.Valid
import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.function.Consumer
import java.util.stream.Collectors

@Service
@RequiredArgsConstructor
class TripScheduleService {
    private val tripScheduleRepository: TripScheduleRepository? = null
    private val memberRepository: MemberRepository? = null
    private val tripInformationService: TripInformationService? = null
    private val tripInformationRepository: TripInformationRepository? = null
    private val authService: AuthService? = null
    private val placeRepository: PlaceRepository? = null

    /**
     * 현재 로그인한 회원객체를 반환하는 메서드
     *
     * @param token JWT 토큰
     * @return 로그인한 회원 객체
     * @throws ServiceException 로그인하지 않은 경우 예외 발생
     */
    fun getLoggedInMember(token: String): Member {
        // 로그인 여부 확인 및 회원 정보 가져오기

        val member = authService!!.getLoggedInMember(token)
            ?: throw ServiceException("401-2", "로그인이 필요합니다.")

        return member
    }

    // 여행 일정 생성
    @Transactional
    fun createSchedule(req: TripScheduleReqDto, token: String): TripScheduleInfoResDto {
        // 로그인한 회원 정보 및 도시 유효성 검증
        val member = getLoggedInMember(token)
        val selectedCity = validateCity(req.cityName!!)

        // 여행 일정 생성 및 저장
        val newSchedule = createAndSaveSchedule(member, req)

        // 세부일정 검증 및 저장
        if (req.tripInformations != null && !req.tripInformations.isEmpty()) {
            validateTripInformations(req.tripInformations, selectedCity)
            tripInformationService!!.addTripInformations(newSchedule, req.tripInformations)
        }

        // 응답 DTO 생성
//        List<TripInformationResDto> tripInfoDtos = buildTripInformationResDtos(req.getTripInformations());
        return TripScheduleInfoResDto(newSchedule)
    }

    // 도시명 유효성 검증
    private fun validateCity(cityName: String): String {
        if (cityName == null || cityName.isEmpty()) {
            throw ServiceException("400-2", "여행지(도시명) 선택은 필수입니다.")
        }
        return cityName
    }


    // 여행 일정을 생성하고 DB에 저장
    private fun createAndSaveSchedule(member: Member, req: TripScheduleReqDto): TripSchedule {
        val schedule = TripSchedule()
        schedule.member = member
        schedule.title = req.title
        schedule.description = req.description
        schedule.startDate = req.startDate
        schedule.endDate = req.endDate

        tripScheduleRepository!!.save(schedule)
        return schedule
    }

    // 각 세부일정의 장소가 선택한 도시와 일치하는지 검증
    private fun validateTripInformations(tripInfos: List<TripInformationReqDto>, selectedCity: String) {
        tripInfos.forEach(Consumer { tripInfo: TripInformationReqDto ->
            val place = placeRepository!!.findById(tripInfo.placeId!!)
                .orElseThrow {
                    ServiceException(
                        "404-2",
                        "해당 장소가 존재하지 않습니다."
                    )
                }
            if (place.cityName != selectedCity) {
                throw ServiceException("400-1", "선택한 도시와 일치하지 않는 장소가 포함되어 있습니다.")
            }
        })
    }

    @get:Transactional(readOnly = true)
    val allSchedules: List<TripScheduleResDto>
        // TripInformation 응답 DTO 리스트 생성(필요없음)
        get() {
            val schedules = tripScheduleRepository!!.findAll()

            if (schedules.isEmpty()) {
                throw ServiceException("404-3", "여행 일정이 존재하지 않습니다.")
            }

            return schedules.stream()
                .map { tripSchedule: TripSchedule? -> TripScheduleResDto(tripSchedule!!) }
                .collect(Collectors.toList())
        }

    // 회원 이름 조회(필요없을듯)
    @Transactional(readOnly = true)
    fun getMemberName(memberId: Long): String {
        return memberRepository!!.findById(memberId)
            .map(Member::username)
            .orElseThrow {
                ServiceException(
                    "404-1",
                    "해당 회원이 존재하지 않습니다."
                )
            }
    }

    // 특정 회원의 여행 일정 조회(필요없을듯)
    @Transactional(readOnly = true)
    fun getSchedulesByMemberId(memberId: Long): List<TripScheduleResDto> {
        // 회원 존재 여부 검증
        if (!memberRepository!!.existsById(memberId)) {
            throw ServiceException("404-1", "해당 회원이 존재하지 않습니다.")
        }

        // 회원 ID로 여행 일정 조회
        val schedules = tripScheduleRepository!!.findByMemberId(memberId)

        // 여행 일정 존재 여부 검증
        if (schedules.isEmpty()) {
            throw ServiceException("404-3", "해당 회원의 여행 일정이 존재하지 않습니다.")
        }

        return schedules.stream()
            .map { tripSchedule: TripSchedule? -> TripScheduleResDto(tripSchedule!!) }
            .collect(Collectors.toList())
    }


    /**
     * 로그인한 회원이 자신의 여행 일정을 삭제하는 메서드
     *
     * @param scheduleId 삭제할 일정 ID
     * @param token      JWT 토큰 (로그인된 사용자 확인)
     * @throws ServiceException 로그인되지 않았거나, 권한이 없는 경우 예외 발생
     */
    @Transactional
    fun deleteSchedule(
        scheduleId: Long,
        token: String
    ) {
        // 로그인한 회원 ID 가져오기

        val member = getLoggedInMember(token)

        val schedule = tripScheduleRepository!!.findById(scheduleId)
            .orElseThrow {
                ServiceException(
                    "404-1",
                    "일정이 존재하지 않습니다."
                )
            }

        // 현재 로그인한 사용자가 일정 생성자인지 확인
        if (schedule.member!!.id != member.id) {
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
        // 여행 일정이 존재하는지 확인
        val schedule = tripScheduleRepository!!.findById(scheduleId)
            .orElseThrow {
                ServiceException(
                    "404-1",
                    "해당 일정이 존재하지 않습니다."
                )
            }

        // 일정 정보 업데이트
        schedule.updateSchedule(req)

        return schedule
    }

    // 여행 일정 및 여행 정보 통합 수정 메서드
    @Transactional
    fun updateTrip(reqDto: @Valid TripUpdateReqDto, token: String): TripUpdateResDto {
        // 로그인한 회원 정보 가져오기
        val member = getLoggedInMember(token)

        // 여행 일정 확인
        val tripSchedule = tripScheduleRepository!!.findById(reqDto.tripScheduleId)
            .orElseThrow {
                ServiceException(
                    "404-1",
                    "해당 일정이 존재하지 않습니다."
                )
            }

        // 현재 로그인한 사용자가 일정 생성자인지 확인
        if (tripSchedule.member!!.id != member.id) {
            throw ServiceException("403-1", "본인이 생성한 일정만 수정할 수 있습니다.")
        }

        // 여행 일정 수정
        tripSchedule.updateSchedule(reqDto.scheduleUpdate)

        // 여행 정보 수정
        val updatedTripInformations = reqDto.tripInformationUpdates.stream()
            .map<TripInformation?> { infoUpdate: TripInformationUpdateReqDto ->
                val tripInfo = tripInformationRepository!!.findById(
                    infoUpdate.tripInformationId!!
                )
                    .orElseThrow {
                        ServiceException(
                            "404-2",
                            "해당 여행 정보가 존재하지 않습니다."
                        )
                    }
                // 본인 확인
                if (tripInfo.tripSchedule.member!!.id != member.id) {
                    throw ServiceException("403-2", "본인이 생성한 일정의 여행 정보만 수정할 수 있습니다.")
                }

                tripInfo.updateTripInformation(infoUpdate)
                tripInfo
            }
            .toList()

        return TripUpdateResDto(tripSchedule, updatedTripInformations)
    }

    // 특정 회원이 생성한 여행 일정 조회
    @Transactional(readOnly = true)
    fun getSchedulesByCreator(token: String): List<TripScheduleResDto> {
        // 로그인한 회원 정보 가져오기

        val member = getLoggedInMember(token)

        // 회원 ID를 기반으로 해당 회원이 만든 여행 일정들을 조회
        val schedules = tripScheduleRepository!!.findByMemberId(member.id!!)

        // 여행 일정이 존재하지 않는 경우 예외 발생
        if (schedules.isEmpty()) {
            throw ServiceException("404-3", "해당 회원의 여행 일정이 존재하지 않습니다.")
        }

        // 조회한 TripSchedule 엔티티 리스트를 TripScheduleResDto 리스트로 변환하여 반환
        return schedules.stream()
            .map { tripSchedule: TripSchedule? -> TripScheduleResDto(tripSchedule!!) }
            .collect(Collectors.toList())
    }

    // 특정 회원이 생성한 여행 일정의 세부 정보 조회
    @Transactional
    fun getTripInfo(token: String, id: Long): List<TripScheduleInfoResDto> {
        // 로그인한 회원 정보 가져오기

        val member = getLoggedInMember(token)

        // 일정 조회
        val schedule = tripScheduleRepository!!.findById(id)
            .orElseThrow {
                ServiceException(
                    "404-1",
                    "해당 일정이 존재하지 않습니다"
                )
            }

        // 본인 확인
        if (schedule.member!!.id != member.id) {
            throw ServiceException("403-1", "본인이 생성한 일정만 조회할 수 있습니다.")
        }

        // 여행 일정에 포함된 여행 정보 조회 후 DTO 변환
//        List<TripInformationResDto> tripInformations = tripInformationRepository.findByTripScheduleId(id)
//                .stream()
//                .map(TripInformationResDto::new) // DTO 변환
//                .collect(Collectors.toList());
        return java.util.List.of(TripScheduleInfoResDto(schedule))
    }
}
