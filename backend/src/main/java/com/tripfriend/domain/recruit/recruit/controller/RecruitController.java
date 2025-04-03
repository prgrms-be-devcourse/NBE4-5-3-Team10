package com.tripfriend.domain.recruit.recruit.controller;

import com.tripfriend.domain.place.place.service.PlaceService;
import com.tripfriend.domain.recruit.recruit.dto.RecruitListResponseDto;
import com.tripfriend.domain.recruit.recruit.dto.RecruitRequestDto;
import com.tripfriend.domain.recruit.recruit.dto.RecruitDetailResponseDto;
import com.tripfriend.domain.recruit.recruit.service.RecruitService;
import com.tripfriend.global.annotation.CheckPermission;
import com.tripfriend.global.dto.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

//    @Operation(summary = "여행지 등록", description = "새로운 여행지를 등록합니다.")
@Tag(name = "Recruit API", description = "동행모집 글 관련 기능을 제공합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/recruits")
public class RecruitController {
    private final RecruitService recruitService;

    @Operation(summary = "동행모집 글 단건조회", description = "id로 동행모집 글을 조회합니다.")
    @GetMapping("/{recruitId}") // 이름 맞춰주기
    public RsData<RecruitDetailResponseDto> getRecruit(@PathVariable("recruitId") Long recruitId) {
        return new RsData<>("200-3", "동행 모집 글이 성공적으로 조회되었습니다.", recruitService.findById(recruitId));
    }

    @Operation(summary = "동행모집 글 목록조회", description = "동행모집 글 목록을 조회합니다.")
    @GetMapping
    public RsData<List<RecruitListResponseDto>> getRecruits(){
        return new RsData<>("200-3", "동행 모집 글 목록이 성공적으로 조회되었습니다.", recruitService.findAll());
    }

    @Operation(summary = "동행모집 글 최신순 3개 조회", description = "최근 3개의 동행모집 글을 조회합니다.")
    @GetMapping("/recent3")
    public RsData<List<RecruitListResponseDto>> getRecent3Recruits(){
        return new RsData<>("200-3", "최근 동행 모집 글 목록이 성공적으로 조회되었습니다.", recruitService.findRecent3());
    }

    @Operation(summary = "동행모집 글 키워드 검색", description = "제목&내용 키워드로 동행모집 글을 검색합니다.")
    @GetMapping("/search")
    public RsData<List<RecruitListResponseDto>> searchRecruits(@RequestParam("keyword") String keyword){
        return new RsData<>("200-3", "동행 모집 글이 제목과 내용으로 성공적으로 검색되었습니다.", recruitService.searchRecruits(keyword));
    }

    @Operation(summary = "동행모집 글 모집여부 검색", description = "모집 여부로 동행모집 글을 검색합니다.")
    @GetMapping("/search2")
    public RsData<List<RecruitListResponseDto>> findRecruitsByIsClosed(@RequestParam("isClosed") Boolean isClosed){
        return new RsData<>("200-3", "동행 모집 글이 모집여부로 성공적으로 검색되었습니다.", recruitService.searchByIsClosed(isClosed));
    }

    @Operation(summary = "동행모집 글 여러 조건 검색 & 정렬", description = "여러 조건으로 동행모집 글을 검색 및 정렬합니다.")
    @GetMapping("/search3")
    public RsData<List<RecruitListResponseDto>> searchAndFilter(
            @RequestParam(name = "keyword") Optional<String> keyword,
            @RequestParam(name = "cityName") Optional<String> cityName,
            @RequestParam(name = "isClosed") Optional<Boolean> isClosed,
            @RequestParam(name = "startDate") Optional<LocalDate> startDate,
            @RequestParam(name = "endDate") Optional<LocalDate> endDate,
            @RequestParam(name = "travelStyle") Optional<String> travelStyle,
            @RequestParam(name = "sameGender") Optional<Boolean> sameGender,
            @RequestParam(name = "sameAge") Optional<Boolean> sameAge,
            @RequestParam(name = "minBudget") Optional<Integer> minBudget,
            @RequestParam(name = "maxBudget") Optional<Integer> maxBudget,
            @RequestParam(name = "minGroupSize") Optional<Integer> minGroupSize,
            @RequestParam(name = "maxGroupSize") Optional<Integer> maxGroupSize,
            @RequestParam(name = "sortBy") Optional<String> sortBy,
            @RequestHeader(value = "Authorization", required = false) String token
    ) {

        return new RsData<>("200-3", "동행 모집 글이 여러 조건으로 성공적으로 검색되었습니다.", recruitService.searchAndFilter(
                keyword, cityName, isClosed, startDate, endDate,
                travelStyle, sameGender, sameAge, minBudget, maxBudget, minGroupSize, maxGroupSize, sortBy, token
        ));
    }

    @Operation(summary = "동행모집 글 등록", description = "동행모집 글을 등록합니다.")
    @PostMapping
    public RsData<RecruitDetailResponseDto> createRecruit (@RequestBody RecruitRequestDto requestDto, @RequestHeader(value = "Authorization", required = false) String token) {
        return new RsData<>("201-3", "동행 모집 글이 성공적으로 등록되었습니다.", recruitService.create(requestDto, token));
    }

    @Operation(summary = "동행모집 글 수정", description = "동행모집 글을 수정합니다.")
    @PutMapping("/{recruitId}")// 일단 put으로 통일
    public RsData<RecruitDetailResponseDto> updateRecruit(@PathVariable("recruitId") Long recruitId, @RequestBody RecruitRequestDto requestDto, @RequestHeader(value = "Authorization", required = false) String token) {
        return new RsData<>("200-3", "동행 모집 글이 성공적으로 수정되었습니다.", recruitService.update(recruitId, requestDto, token));
    }

    @Operation(summary = "동행모집 글 삭제", description = "동행모집 글을 삭제합니다.")
    @DeleteMapping("/{recruitId}")
    public RsData<Void> deleteRecruit(@PathVariable("recruitId") Long recruitId, @RequestHeader(value = "Authorization", required = false) String token){ // 이름 명시
        recruitService.delete(recruitId, token);
        return new RsData<>("200-3", "동행 모집 글이 성공적으로 삭제되었습니다.");
    }
}
