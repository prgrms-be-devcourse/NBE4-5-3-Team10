package com.tripfriend.domain.trip.information.controller;

import com.tripfriend.domain.trip.information.dto.TripInformationReqDto;
import com.tripfriend.domain.trip.information.dto.TripInformationResDto;
import com.tripfriend.domain.trip.information.dto.TripInformationUpdateReqDto;
import com.tripfriend.domain.trip.information.dto.VisitedReqDto;
import com.tripfriend.domain.trip.information.entity.TripInformation;
import com.tripfriend.domain.trip.information.service.TripInformationService;
import com.tripfriend.global.dto.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/trip/information")
@Tag(name = "TripInformation API", description = "여행 세부 일정 관련 기능을 제공합니다.")
public class TripInformationController {
    private final TripInformationService tripInformationService;


    // 세부 일정 조회
    @GetMapping("/{id}")
    @Transactional
    @Operation(summary = "나의 세부일정 조회", description = "회원 토큰과 세부일정 id를 통해 해당 회원의 세부일정을 조회 할 수 있습니다.")
    public RsData<TripInformationResDto> getTripInformation(
            @Parameter(
                    description = "세부 일정 id",
                    example = "1"
            ) @PathVariable Long id,
            @RequestHeader("Authorization") String token) {

        TripInformation tripInformation = tripInformationService.getTripInformation(id, token);
        TripInformationResDto infoDto = new TripInformationResDto(tripInformation);
        return new RsData<>(
                "200-1",
                "여행 정보 조회 성공",
                infoDto
        );
    }

    // 특정 여행 정보를 수정
    @PutMapping("/{id}")
    @Transactional
    @Operation(summary = "나의 세부일정 수정", description = "회원 토큰과 세부일정 id를 통해 해당 회원의 세부일정을 수정 할 수 있습니다.")
    public RsData<TripInformationResDto> updateTripInformation(
            @Parameter(
                    description = "세부 일정 id",
                    example = "1"
            ) @PathVariable Long id,
            @RequestBody @Valid TripInformationUpdateReqDto request) {

        request.setTripInformationId(id);
        TripInformation updatedTripInfo = tripInformationService.updateTripInformation(id, request);
        TripInformationResDto resDto = new TripInformationResDto(updatedTripInfo);// 반환 DTO
        return new RsData<>(
                "200-3",
                "여행 정보가 성공적으로 수정되었습니다.",
                resDto
        );
    }

    // 세부 일정 등록
    @PostMapping
    @Transactional
    @Operation(summary = "나의 세부일정 등록", description = "회원 토큰을 확인 하고 세부일정을 등록 할 수 있습니다.")
    public RsData<TripInformationResDto> createTripInformation(
            @Valid @RequestBody TripInformationReqDto reqDto,
            @RequestHeader("Authorization") String token) {
        TripInformationResDto resDto = tripInformationService.addTripInformation(reqDto, token);
        return new RsData<>(
                "200-2",
                "세부 일정 등록 성공",
                resDto
        );
    }

    // 세부 일정 삭제
    @DeleteMapping("/{tripInformationId}")
    @Operation(summary = "나의 세부일정 삭제", description = "회원 토큰과 세부일정 id를 통해 해당 회원의 세부일정을 삭제 할 수 있습니다.")
    public RsData<Void> deleteTripInformation(
            @PathVariable Long tripInformationId,
            @RequestHeader("Authorization") String token) {
        tripInformationService.deleteTripInformation(tripInformationId, token);
        return new RsData<>(
                "200-4",
                "세부 일정 삭제 성공"
        );
    }

    // 방문 여부 변경
    @PutMapping("/update-visited")
    @Operation(summary = "세부일정 방문 여부 변경", description = "회원 토큰을 확인하고 세부일정의 방문 여부를 변경 할 수 있습니다.")
    public RsData<Void> updateVisited(
            @Valid @RequestBody VisitedReqDto reqDto,
            @RequestHeader("Authorization") String token) {
        tripInformationService.updateVisited(reqDto, token);
        return new RsData<>("200-5",
                "방문 여부 업데이트 성공"
        );
    }

}
