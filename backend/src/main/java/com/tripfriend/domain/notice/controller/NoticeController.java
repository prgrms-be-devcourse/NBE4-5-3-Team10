package com.tripfriend.domain.notice.controller;

import com.tripfriend.domain.notice.dto.Dto;

import com.tripfriend.domain.notice.entity.Notice;
import com.tripfriend.domain.notice.repository.NoticeRepository;
import com.tripfriend.domain.notice.service.NoticeService;
import com.tripfriend.global.annotation.CheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Notice API", description = "공지사항 관련 API입니다.")
@RestController
@RequestMapping
@RequiredArgsConstructor
public class NoticeController {
    private final NoticeRepository noticeRepository;
    private final NoticeService noticeService;

    //공지사항 생성
    @Operation(summary = "공지사항 생성", description = "관리자가 공지사항을 등록합니다.")
    @PostMapping("/admin/notice")
    @CheckPermission("ADMIN") // 관리자 권한
    public ResponseEntity<Notice> createNotice(@RequestBody Notice notice) {
        return ResponseEntity.ok(noticeService.createNotice(notice.getTitle(), notice.getContent()));
    }

    //관리자 공지 조회
    @Operation(summary = "공지사항 전체 조회 (관리자)", description = "관리자가 전체 공지사항 목록을 조회합니다.")
    @GetMapping("/admin/notice")
    @CheckPermission("ADMIN")
    public ResponseEntity<List<Notice>> getAllNoticesForAdmin() {
        return ResponseEntity.ok(noticeRepository.findAll());
    }


    //공지사항 전체 조회
    @Operation(summary = "공지사항 전체 조회", description = "모든 사용자가 볼 수 있는 공지사항 목록을 조회합니다.")
    @GetMapping("/notice")
    public ResponseEntity<List<Notice>> getAllNotices() {
        return ResponseEntity.ok(noticeRepository.findAll());
    }

    // 공지사항 검색 조회
    @Operation(summary = "공지사항 단건 조회", description = "ID로 특정 공지사항을 조회합니다. (관리자용)")
    @GetMapping("/admin/notice/{id}")
    public ResponseEntity<Notice> getNoticeById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(noticeService.getNoticeById(id));
    }

    //공지사항 수정
    @Operation(summary = "공지사항 수정", description = "관리자가 특정 공지사항을 수정합니다.")
    @PutMapping("/admin/notice/{id}")
    @CheckPermission("ADMIN")
    public ResponseEntity<Notice> updateNotice(
            @PathVariable("id") Long id,
            @RequestBody Dto request) {

        return ResponseEntity.ok(
                noticeService.updateNoticeById(id, request.getTitle(), request.getContent())
        );
    }

    //공지사항 삭제
    @Operation(summary = "공지사항 삭제", description = "관리자가 특정 공지사항을 삭제합니다.")
    @DeleteMapping("/admin/notice/{id}")
    @CheckPermission("ADMIN")
    public ResponseEntity<Void> deleteNotice
    (@PathVariable("id") Long id) {
        noticeService.deleteNotice(id);
        return ResponseEntity.noContent().build();
    }
}
