package com.tripfriend.domain.trip.information.controller

import com.tripfriend.domain.trip.information.dto.TripInformationReqDto
import com.tripfriend.domain.trip.information.dto.TripInformationResDto
import com.tripfriend.domain.trip.information.dto.TripInformationUpdateReqDto
import com.tripfriend.domain.trip.information.dto.VisitedReqDto
import com.tripfriend.domain.trip.information.service.TripInformationService
import com.tripfriend.global.dto.RsData
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/trip/information")
@Tag(name = "TripInformation API", description = "여행 세부 일정 관련 기능을 제공합니다.")
class TripInformationController(
    private val tripInformationService: TripInformationService
) {

    // 세부 일정 조회
    @GetMapping("/{id}")
    @Transactional
    @Operation(summary = "나의 세부일정 조회", description = "회원 토큰과 세부일정 id를 통해 해당 회원의 세부일정을 조회할 수 있습니다.")
    fun getTripInformation(
        @Parameter(description = "세부 일정 id", example = "1") @PathVariable id: Long,
        @RequestHeader("Authorization") token: String
    ): RsData<TripInformationResDto> {
        val tripInformation = tripInformationService.getTripInformation(id, token)
        val infoDto = TripInformationResDto(tripInformation)
        return RsData("200-1", "여행 정보 조회 성공", infoDto)
    }

    // 특정 여행 정보를 수정
    @PutMapping("/{id}")
    @Transactional
    @Operation(summary = "나의 세부일정 수정", description = "회원 토큰과 세부일정 id를 통해 해당 회원의 세부일정을 수정할 수 있습니다.")
    fun updateTripInformation(
        @Parameter(description = "세부 일정 id", example = "1") @PathVariable id: Long,
        @RequestBody @Valid request: TripInformationUpdateReqDto
    ): RsData<TripInformationResDto> {
        request.tripInformationId = id
        val updatedTripInfo = tripInformationService.updateTripInformation(id, request)
        val resDto = TripInformationResDto(updatedTripInfo)
        return RsData("200-3", "여행 정보가 성공적으로 수정되었습니다.", resDto)
    }

    // 세부 일정 등록
    @PostMapping
    @Transactional
    @Operation(summary = "나의 세부일정 등록", description = "회원 토큰을 확인하고 세부일정을 등록할 수 있습니다.")
    fun createTripInformation(
        @RequestBody @Valid reqDto: TripInformationReqDto,
        @RequestHeader("Authorization") token: String
    ): RsData<TripInformationResDto> {
        val resDto = tripInformationService.addTripInformation(reqDto, token)
        return RsData("200-2", "세부 일정 등록 성공", resDto)
    }

    // 세부 일정 삭제
    @DeleteMapping("/{tripInformationId}")
    @Operation(summary = "나의 세부일정 삭제", description = "회원 토큰과 세부일정 id를 통해 해당 회원의 세부일정을 삭제할 수 있습니다.")
    fun deleteTripInformation(
        @PathVariable tripInformationId: Long,
        @RequestHeader("Authorization") token: String
    ): RsData<Void> {
        tripInformationService.deleteTripInformation(tripInformationId, token)
        return RsData("200-4", "세부 일정 삭제 성공")
    }

    // 방문 여부 변경
    @PutMapping("/update-visited")
    @Operation(summary = "세부일정 방문 여부 변경", description = "회원 토큰을 확인하고 세부일정의 방문 여부를 변경할 수 있습니다.")
    fun updateVisited(
        @RequestBody @Valid reqDto: VisitedReqDto,
        @RequestHeader("Authorization") token: String
    ): RsData<Void> {
        tripInformationService.updateVisited(reqDto, token)
        return RsData("200-5", "방문 여부 업데이트 성공")
    }
}