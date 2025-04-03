package com.tripfriend.domain.blacklist.controller;

import com.tripfriend.domain.blacklist.dto.Dto;
import com.tripfriend.domain.blacklist.entity.Blacklist;
import com.tripfriend.domain.blacklist.service.BlacklistService;
import com.tripfriend.global.annotation.CheckPermission;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/blacklist")
@RequiredArgsConstructor
@Tag(name = "Blacklist API", description = "관리자가 블랙리스트를 관리할 수 있는 기능을 제공합니다.")
public class BlacklistController {
    private final BlacklistService blacklistService;

    //블랙리스트 추가
    @Operation(summary = "블랙리스트 추가", description = "특정 사용자를 블랙리스트에 등록합니다.")
    @PostMapping
    @CheckPermission("ADMIN")
    public ResponseEntity<String> addToBlacklist(@RequestBody Dto requestDto) {
        blacklistService.addToBlacklist(requestDto.getMemberId(), requestDto.getReason());
        return ResponseEntity.ok("블랙리스트 추가 완료");
    }

    //블랙리스트 삭제
    @Operation(summary = "블랙리스트 삭제", description = "블랙리스트에 등록된 사용자를 삭제합니다.")
    @DeleteMapping("/{memberId}")
    @CheckPermission("ADMIN")
    public ResponseEntity<String> removeFromBlacklist(@PathVariable("memberId") Long memberId) {
        blacklistService.removeFromBlacklist(memberId);
        return ResponseEntity.ok("블랙리스트 삭제 완료");
    }

    //블랙리스트 조회
    @Operation(summary = "블랙리스트 목록 조회", description = "블랙리스트에 등록된 모든 사용자를 조회합니다.")
    @GetMapping
    @CheckPermission("ADMIN")
    public ResponseEntity<List<Blacklist>> getBlacklist() {
        return ResponseEntity.ok(blacklistService.getBlacklist());
    }


}
