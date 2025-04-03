package com.tripfriend.domain.recruit.apply.controller;

import com.tripfriend.domain.recruit.apply.dto.ApplyCreateRequestDto;
import com.tripfriend.domain.recruit.apply.dto.ApplyResponseDto;
import com.tripfriend.domain.recruit.apply.service.ApplyService;
import com.tripfriend.global.dto.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Apply API", description = "동행모집 댓글 관련 기능을 제공합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/recruits/{recruitId}/applies") // requestmapping에 url 써야함
public class ApplyController {
    private final ApplyService applyService;

    @Operation(summary = "동행모집 댓글 목록 조회", description = "동행모집 댓글 목록을 조회합니다.")
    @GetMapping
    RsData<List<ApplyResponseDto>> getApplies(@PathVariable("recruitId") Long recruitId) {
        return new RsData<>("200-4", "동행 요청 댓글 목록이 성공적으로 조회되었습니다.", applyService.findByRecruitId(recruitId)); // 인자 주기
    }

    @Operation(summary = "동행모집 댓글 등록", description = "동행모집 댓글을 등록합니다.")
    @PostMapping
    RsData<ApplyResponseDto> createApply(@PathVariable("recruitId") Long recruitId, @RequestBody ApplyCreateRequestDto requestDto, @RequestHeader(value = "Authorization", required = false) String token){
        return new RsData<>("201-4", "동행 요청 댓글이 성공적으로 등록되었습니다.", applyService.create(recruitId, requestDto, token));
    }

    @Operation(summary = "동행모집 댓글 삭제", description = "동행모집 댓글을 삭제합니다.")
    @DeleteMapping("/{applyId}")
    RsData<Void> deleteApply(@PathVariable("applyId") Long applyId, @RequestHeader(value = "Authorization", required = false) String token) {
        applyService.delete(applyId, token);
        return new RsData<>("200-4", "동행 요청 댓글이 성공적으로 삭제되었습니다.");
    }

}
