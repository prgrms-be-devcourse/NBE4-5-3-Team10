package com.tripfriend.domain.notice.service

import com.tripfriend.domain.notice.entity.Notice
import com.tripfriend.domain.notice.repository.NoticeRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class NoticeService(
    private val noticeRepository: NoticeRepository
) {
    // 공지사항 생성
    fun createNotice(title: String, content: String): Notice {
        val notice = Notice(title = title, content = content)
        return noticeRepository.save(notice)
    }

    // 전체 공지사항 조회
    fun getAllNotices(): List<Notice> = noticeRepository.findAll()

    // 단건 공지사항 조회
    fun getNoticeById(id: Long): Notice =
        noticeRepository.findById(id)
            .orElseThrow { IllegalArgumentException("해당 공지사항을 찾을 수 없습니다. id: $id") }


    // 공지사항 수정
    @Transactional
    fun updateNoticeById(id: Long, title: String, content: String): Notice {
        val notice = getNoticeById(id)
        notice.update(title, content)
        return notice
    }


    // 공지사항 삭제
    fun deleteNotice(id: Long) {
        val notice = getNoticeById(id)
        noticeRepository.delete(notice)
    }
}
