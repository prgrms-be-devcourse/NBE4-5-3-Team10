package com.tripfriend.domain.trip.schedule.controller

import com.tripfriend.domain.trip.schedule.dto.*
import com.tripfriend.domain.trip.schedule.service.TripScheduleService
import com.tripfriend.global.annotation.CheckPermission
import com.tripfriend.global.dto.RsData
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/trip/schedule")
@Tag(name = "TripSchedule API", description = "여행일정 관련 기능을 제공합니다.")
class TripScheduleController(
    private val scheduleService: TripScheduleService
) {

    @PostMapping
    @Operation(summary = "나의 여행 일정 등록", description = "로그인된 회원의 token 값을 기반으로 새로운 일정을 생성합니다.")
    fun createSchedule(
        @RequestBody reqBody: TripScheduleReqDto,
        @RequestHeader(value = "Authorization", required = false) token: String
    ): RsData<TripScheduleInfoResDto> {
        val schedule = scheduleService.createSchedule(reqBody, token)
        return RsData("200-1", "일정이 성공적으로 생성되었습니다.", schedule)
    }

    @GetMapping
    @Operation(hidden = true)
    @CheckPermission("ADMIN")
    fun allSchedules(): RsData<List<TripScheduleResDto>> {
        val schedules = scheduleService.allSchedules
        return RsData("200-2", "전체 일정을 성공적으로 조회했습니다.", schedules)
    }

    @GetMapping("/my-schedules")
    @Operation(summary = "나의 여행 일정 전체 조회")
    fun getMySchedules(
        @RequestHeader(value = "Authorization", required = false) token: String
    ): RsData<List<TripScheduleResDto>> {
        val schedules = scheduleService.getSchedulesByCreator(token)
        val memberName = schedules.firstOrNull()?.memberName ?: "회원"
        return RsData("200-3", "$memberName 님이 생성한 일정 조회가 완료되었습니다.", schedules)
    }

    @GetMapping("/my-schedules/{id}")
    @Operation(summary = "나의 여행 일정 세부 조회")
    fun getMyTripInfo(
        @RequestHeader(value = "Authorization", required = false) token: String,
        @PathVariable id: Long
    ): RsData<List<TripScheduleInfoResDto>> {
        val schedules = scheduleService.getTripInfo(token, id)
        val memberName = schedules.firstOrNull()?.memberName ?: "회원"
        return RsData("200-4", "$memberName 님이 생성한 일정 조회가 완료되었습니다.", schedules)
    }

    @GetMapping("/member/{memberId}")
    @CheckPermission("ADMIN")
    @Operation(hidden = true)
    fun getSchedulesByMember(@PathVariable memberId: Long): RsData<List<TripScheduleResDto>> {
        val memberName = scheduleService.getMemberName(memberId)
        val schedules = scheduleService.getSchedulesByMemberId(memberId)
        return RsData("200-3", "$memberName 님의 일정을 성공적으로 조회했습니다.", schedules)
    }

    @DeleteMapping("/my-schedules/{scheduleId}")
    @Operation(summary = "나의 여행 일정 삭제", description = "나의 일정을 삭제할 수 있다.")
    fun deleteSchedule(
        @PathVariable scheduleId: Long,
        @RequestHeader(value = "Authorization", required = false) token: String
    ): RsData<Void> {
        scheduleService.deleteSchedule(scheduleId, token)
        return RsData("200-5", "일정이 성공적으로 삭제되었습니다.")
    }

    @PutMapping("/{scheduleId}")
    @CheckPermission("ADMIN")
    @Operation(hidden = true)
    fun updateSchedule(
        @PathVariable scheduleId: Long,
        @RequestBody @Valid req: TripScheduleUpdateReqDto
    ): RsData<TripScheduleResDto> {
        val updatedSchedule = scheduleService.updateSchedule(scheduleId, req)
        val resDto = TripScheduleResDto(updatedSchedule)
        return RsData("200-5", "일정이 성공적으로 수정되었습니다.", resDto)
    }

    @PutMapping("/update")
    @Operation(summary = "나의 여행 일정 및 여행 정보 통합 수정", hidden = true)
    fun updateTrip(
        @RequestBody @Valid reqDto: TripUpdateReqDto,
        @RequestHeader(value = "Authorization", required = false) token: String
    ): RsData<TripUpdateResDto> {
        val resTrip = scheduleService.updateTrip(reqDto, token)
        return RsData("200-1", "여행 일정 및 여행 정보가 성공적으로 수정되었습니다.", resTrip)
    }
}