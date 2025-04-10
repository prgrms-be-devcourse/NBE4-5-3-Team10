package com.tripfriend.domain.trip.information.service

import com.tripfriend.domain.member.member.entity.Member
import com.tripfriend.domain.member.member.service.AuthService
import com.tripfriend.domain.place.place.entity.Place
import com.tripfriend.domain.place.place.repository.PlaceRepository
import com.tripfriend.domain.trip.information.dto.TripInformationReqDto
import com.tripfriend.domain.trip.information.dto.TripInformationResDto
import com.tripfriend.domain.trip.information.dto.TripInformationUpdateReqDto
import com.tripfriend.domain.trip.information.dto.VisitedReqDto
import com.tripfriend.domain.trip.information.entity.TripInformation
import com.tripfriend.domain.trip.information.repository.TripInformationRepository
import com.tripfriend.domain.trip.schedule.entity.TripSchedule
import com.tripfriend.domain.trip.schedule.repository.TripScheduleRepository
import com.tripfriend.global.exception.ServiceException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TripInformationService(
    private val tripInformationRepository: TripInformationRepository,
    private val tripScheduleRepository: TripScheduleRepository,
    private val placeRepository: PlaceRepository,
    private val authService: AuthService
) {

    /**
     * 현재 로그인한 회원 객체를 반환한다.
     * @throws ServiceException 로그인하지 않은 경우 예외 발생
     */
    fun getLoggedInMember(token: String): Member {
        return authService.getLoggedInMember(token)
            ?: throw ServiceException("401-2", "로그인이 필요합니다.")
    }

    /**
     * 요청 DTO로부터 장소 ID를 확인 후 장소 객체를 반환한다.
     */
    fun getPlace(reqDto: TripInformationReqDto): Place {
        val placeId = reqDto.placeId ?: throw ServiceException("400-2", "장소 ID가 누락되었습니다.")
        return placeRepository.findById(placeId).orElseThrow {
            ServiceException("404-2", "해당 장소가 존재하지 않습니다.")
        }
    }

    /**
     * 여행 정보 ID와 토큰을 기반으로 회원 및 여행정보의 소유권을 검증한다.
     */
    fun validateTripInformation(tripInfoId: Long, token: String): TripInformation {
        val member = getLoggedInMember(token)
        val tripInformation = tripInformationRepository.findById(tripInfoId).orElseThrow {
            ServiceException("404-2", "해당 여행 정보가 존재하지 않습니다.")
        }
        if (tripInformation.tripSchedule.member?.id != member.id) {
            throw ServiceException("403-1", "본인이 생성한 일정의 여행 정보만 수정할 수 있습니다.")
        }
        return tripInformation
    }

    /**
     * 여행 정보를 등록하고 DTO를 반환한다.
     */
    fun addTripInformation(reqDto: TripInformationReqDto, token: String): TripInformationResDto {
        val member = getLoggedInMember(token)
        val schedule = tripScheduleRepository.findById(
            reqDto.tripScheduleId ?: throw ServiceException("400", "여행 일정 ID가 누락되었습니다.")
        ).orElseThrow {
            ServiceException("404", "여행 일정이 존재하지 않습니다.")
        }
        if (schedule.member?.id != member.id) {
            throw ServiceException("403", "본인이 생성한 일정만 수정할 수 있습니다.")
        }
        val place = getPlace(reqDto)
        val information = TripInformation(
            tripSchedule = schedule,
            place = place,
            visitTime = reqDto.visitTime,
            duration = reqDto.duration,
            cost = reqDto.cost,
            notes = reqDto.notes,
            transportation = reqDto.transportation,
        )
        tripInformationRepository.save(information)
        return TripInformationResDto(information)
    }

    /**
     * 여행 정보 리스트를 등록하고 상위 일정에 추가한다.
     */
    @Transactional
    fun addTripInformations(schedule: TripSchedule, tripInfoReqs: List<TripInformationReqDto>?) {
        if (tripInfoReqs.isNullOrEmpty()) return

        val tripInformations = tripInfoReqs.map { infoReq ->
            TripInformation(
                tripSchedule = schedule,
                place = getPlace(infoReq),
                visitTime = infoReq.visitTime,
                duration = infoReq.duration,
                transportation = infoReq.transportation,
                cost = infoReq.cost,
                notes = infoReq.notes,
            )
        }

        tripInformationRepository.saveAll(tripInformations)
        schedule.addTripInformations(tripInformations)
    }

    /**
     * 특정 여행 정보를 수정한다.
     */
    @Transactional
    fun updateTripInformation(tripInfoId: Long, req: TripInformationUpdateReqDto): TripInformation {
        val tripInformation = tripInformationRepository.findById(tripInfoId).orElseThrow {
            ServiceException("404-3", "해당 여행 정보가 존재하지 않습니다.")
        }
        tripInformation.updateTripInformation(req)
        return tripInformation
    }

    /**
     * 여행 정보를 삭제한다.
     */
    @Transactional
    fun deleteTripInformation(tripInformationId: Long, token: String) {
        val tripInformation = validateTripInformation(tripInformationId, token)
        tripInformationRepository.delete(tripInformation)
    }

    /**
     * 방문 여부를 업데이트한다.
     */
    @Transactional
    fun updateVisited(req: VisitedReqDto, token: String) {
        val tripInformation = validateTripInformation(req.tripInformationId, token)
        tripInformation.isVisited = req.isVisited
        tripInformationRepository.save(tripInformation)
    }

    /**
     * 세부 일정을 조회한다.
     */
    @Transactional
    fun getTripInformation(id: Long, token: String): TripInformation {
        return validateTripInformation(id, token)
    }
}