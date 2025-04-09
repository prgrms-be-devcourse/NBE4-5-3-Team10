package com.tripfriend.domain.notice.controller

import com.tripfriend.domain.notice.dto.Dto
import com.tripfriend.domain.notice.entity.Notice
import com.tripfriend.domain.notice.service.NoticeService
import com.tripfriend.global.annotation.CheckPermission
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Notice API", description = "공지사항 관련 API입니다.")
@RestController
@RequestMapping
class NoticeController(
    private val noticeService: NoticeService
) {

    //공지사항 생성
    @Operation(summary = "공지사항 생성", description = "관리자가 공지사항을 등록합니다.")
    @PostMapping("/admin/notice")
    @CheckPermission("ADMIN")
    fun createNotice(@RequestBody dto: Dto): ResponseEntity<Notice> {
        val saved = noticeService.createNotice(dto.title, dto.content)
        return ResponseEntity.ok(saved)
    }
    @GetMapping("/admin/notice")
    @CheckPermission("ADMIN")
    @Operation(summary = "공지사항 전체 조회 (관리자)", description = "관리자가 전체 공지사항 목록을 조회합니다.")
    fun getAllNoticesForAdmin(): ResponseEntity<List<Notice>> {
        val notices = noticeService.getAllNotices()
        return ResponseEntity.ok(notices)
    }



    @GetMapping("/notice")
    @Operation(summary = "공지사항 전체 조회", description = "모든 사용자가 볼 수 있는 공지사항 목록을 조회합니다.")
    fun getAllNotices(): ResponseEntity<List<Notice>> {
        val notices = noticeService.getAllNotices()
        return ResponseEntity.ok(notices)
    }

    // 공지사항 검색 조회
    @GetMapping("/admin/notice/{id}")
    @Operation(summary = "공지사항 단건 조회", description = "ID로 특정 공지사항을 조회합니다. (관리자용)")
    fun getNoticeById(@PathVariable id: Long): ResponseEntity<Notice> {
        val notice = noticeService.getNoticeById(id)
        return ResponseEntity.ok(notice)
    }

    //공지사항 수정
    @PutMapping("/admin/notice/{id}")
    @CheckPermission("ADMIN")
    @Operation(summary = "공지사항 수정", description = "관리자가 특정 공지사항을 수정합니다.")
    fun updateNotice(
        @PathVariable id: Long,
        @RequestBody request: Dto
    ): ResponseEntity<Notice> {
        val updated = noticeService.updateNoticeById(id, request.title, request.content)
        return ResponseEntity.ok(updated)
    }

    //공지사항 삭제
    @DeleteMapping("/admin/notice/{id}")
    @CheckPermission("ADMIN")
    @Operation(summary = "공지사항 삭제", description = "관리자가 특정 공지사항을 삭제합니다.")
    fun deleteNotice(@PathVariable id: Long): ResponseEntity<Void> {
        noticeService.deleteNotice(id)
        return ResponseEntity.noContent().build()
    }
}
