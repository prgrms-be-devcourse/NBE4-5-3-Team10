package com.tripfriend.domain.recruit.recruit.controller

import com.tripfriend.domain.recruit.recruit.dto.RecruitDetailResponseDto
import com.tripfriend.domain.recruit.recruit.dto.RecruitListResponseDto
import com.tripfriend.domain.recruit.recruit.dto.RecruitRequestDto
import com.tripfriend.domain.recruit.recruit.service.RecruitService
import com.tripfriend.global.dto.RsData
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.*

//    @Operation(summary = "여행지 등록", description = "새로운 여행지를 등록합니다.")
@Tag(name = "Recruit API", description = "동행모집 글 관련 기능을 제공합니다.")
@RestController
@RequestMapping("/recruits")
class RecruitController (private val recruitService: RecruitService)
{

    @Operation(summary = "동행모집 글 단건조회", description = "id로 동행모집 글을 조회합니다.")
    @GetMapping("/{recruitId}") // 이름 맞춰주기
    fun getRecruit(@PathVariable("recruitId") recruitId: Long): RsData<RecruitDetailResponseDto> {
        return RsData("200-3", "동행 모집 글이 성공적으로 조회되었습니다.", recruitService.findById(recruitId))
    }

    @GetMapping
    @Operation(summary = "동행모집 글 목록조회", description = "동행모집 글 목록을 조회합니다.")
    fun getRecruits(): RsData<List<RecruitListResponseDto>> = RsData(
            "200-3",
            "동행 모집 글 목록이 성공적으로 조회되었습니다.",
            recruitService.findAll()
        )

    @GetMapping("/recent3")
    @Operation(summary = "동행모집 글 최신순 3개 조회", description = "최근 3개의 동행모집 글을 조회합니다.")
    fun recent3Recruits(): RsData<List<RecruitListResponseDto>> = RsData(
            "200-3",
            "최근 동행 모집 글 목록이 성공적으로 조회되었습니다.",
            recruitService.findRecent3()
        )

    @Operation(summary = "동행모집 글 키워드 검색", description = "제목&내용 키워드로 동행모집 글을 검색합니다.")
    @GetMapping("/search")
    fun searchRecruits(@RequestParam("keyword") keyword: String): RsData<List<RecruitListResponseDto>> = RsData("200-3", "동행 모집 글이 제목과 내용으로 성공적으로 검색되었습니다.", recruitService.searchRecruits(keyword))

    @Operation(summary = "동행모집 글 모집여부 검색", description = "모집 여부로 동행모집 글을 검색합니다.")
    @GetMapping("/search2")
    fun findRecruitsByIsClosed(@RequestParam("isClosed") isClosed: Boolean): RsData<List<RecruitListResponseDto>> = RsData("200-3", "동행 모집 글이 모집여부로 성공적으로 검색되었습니다.", recruitService.searchByIsClosed(isClosed))

    @Operation(summary = "동행모집 글 여러 조건 검색 & 정렬", description = "여러 조건으로 동행모집 글을 검색 및 정렬합니다.")
    @GetMapping("/search3")
    fun searchAndFilter(
        @RequestParam(name = "keyword") keyword: Optional<String>,
        @RequestParam(name = "cityName") cityName: Optional<String>,
        @RequestParam(name = "isClosed") isClosed: Optional<Boolean>,
        @RequestParam(name = "startDate") startDate: Optional<LocalDate>,
        @RequestParam(name = "endDate") endDate: Optional<LocalDate>,
        @RequestParam(name = "travelStyle") travelStyle: Optional<String>,
        @RequestParam(name = "sameGender") sameGender: Optional<Boolean>,
        @RequestParam(name = "sameAge") sameAge: Optional<Boolean>,
        @RequestParam(name = "minBudget") minBudget: Optional<Int>,
        @RequestParam(name = "maxBudget") maxBudget: Optional<Int>,
        @RequestParam(name = "minGroupSize") minGroupSize: Optional<Int>,
        @RequestParam(name = "maxGroupSize") maxGroupSize: Optional<Int>,
        @RequestParam(name = "sortBy") sortBy: Optional<String>,
        @RequestHeader(value = "Authorization", required = false) token: String?
    ): RsData<List<RecruitListResponseDto>> =
        RsData(
            "200-3", "동행 모집 글이 여러 조건으로 성공적으로 검색되었습니다.",
            recruitService.searchAndFilter(
                keyword, cityName, isClosed, startDate, endDate,
                travelStyle, sameGender, sameAge, minBudget, maxBudget, minGroupSize, maxGroupSize, sortBy, token
            )
        )


    @Operation(summary = "동행모집 글 등록", description = "동행모집 글을 등록합니다.")
    @PostMapping
    fun createRecruit(
        @RequestBody requestDto: RecruitRequestDto,
        @RequestHeader(value = "Authorization", required = false) token: String
    ): RsData<RecruitDetailResponseDto> = RsData("201-3", "동행 모집 글이 성공적으로 등록되었습니다.", recruitService.create(requestDto, token))

    @Operation(summary = "동행모집 글 수정", description = "동행모집 글을 수정합니다.")
    @PutMapping("/{recruitId}") // 일단 put으로 통일
    fun updateRecruit(
        @PathVariable("recruitId") recruitId: Long,
        @RequestBody requestDto: RecruitRequestDto,
        @RequestHeader(value = "Authorization", required = false) token: String
    ): RsData<RecruitDetailResponseDto> = RsData("200-3", "동행 모집 글이 성공적으로 수정되었습니다.", recruitService.update(recruitId, requestDto, token))

    @Operation(summary = "동행모집 글 삭제", description = "동행모집 글을 삭제합니다.")
    @DeleteMapping("/{recruitId}")
    fun deleteRecruit(
        @PathVariable("recruitId") recruitId: Long,
        @RequestHeader(value = "Authorization", required = false) token: String
    ): RsData<Void> { // 이름 명시
        recruitService.delete(recruitId, token)
        return RsData("200-3", "동행 모집 글이 성공적으로 삭제되었습니다.")
    }
}
