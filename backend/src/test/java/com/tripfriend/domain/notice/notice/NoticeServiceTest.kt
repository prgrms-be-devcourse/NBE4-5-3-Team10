package com.tripfriend.domain.notice.notice

import com.tripfriend.domain.notice.entity.Notice
import com.tripfriend.domain.notice.repository.NoticeRepository
import com.tripfriend.domain.notice.service.NoticeService
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.*

@ExtendWith(SpringExtension::class)
class NoticeServiceTest {

    @MockK
    lateinit var noticeRepository: NoticeRepository

    private lateinit var noticeService: NoticeService

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        noticeService = NoticeService(noticeRepository)
    }

    @Test
    @DisplayName("공지사항 생성 성공")
    fun createNotice_success() {
        // given
        val notice = Notice(title = "제목", content = "내용")
        every { noticeRepository.save(any()) } returns notice

        // when
        val result = noticeService.createNotice("제목", "내용")

        // then
        Assertions.assertEquals("제목", result.title)
        Assertions.assertEquals("내용", result.content)
        verify(exactly = 1) { noticeRepository.save(any()) }
    }

    @Test
    @DisplayName("전체 공지사항 조회 성공")
    fun getAllNotices_success() {
        // given
        val notices = listOf(
            Notice(title = "공지1", content = "내용1"),
            Notice(title = "공지2", content = "내용2")
        )
        every { noticeRepository.findAll() } returns notices

        // when
        val result = noticeService.getAllNotices()

        // then
        Assertions.assertEquals(2, result.size)
        verify(exactly = 1) { noticeRepository.findAll() }
    }

    @Test
    @DisplayName("공지사항 단건 조회 성공")
    fun getNoticeById_success() {
        // given
        val notice = Notice(title = "공지", content = "내용")
        every { noticeRepository.findById(1L) } returns Optional.of(notice)

        // when
        val result = noticeService.getNoticeById(1L)

        // then
        Assertions.assertEquals("공지", result.title)
        verify(exactly = 1) { noticeRepository.findById(1L) }
    }

    @Test
    @DisplayName("공지사항 수정 성공")
    fun updateNotice_success() {
        // given
        val notice = Notice(title = "이전 제목", content = "이전 내용")
        every { noticeRepository.findById(1L) } returns Optional.of(notice)

        // when
        val updated = noticeService.updateNoticeById(1L, "새 제목", "새 내용")

        // then
        Assertions.assertEquals("새 제목", updated.title)
        Assertions.assertEquals("새 내용", updated.content)
        verify(exactly = 1) { noticeRepository.findById(1L) }
    }

    @Test
    @DisplayName("공지사항 삭제 성공")
    fun deleteNotice_success() {
        // given
        val notice = Notice(title = "삭제할 공지", content = "삭제할 내용")
        every { noticeRepository.findById(1L) } returns Optional.of(notice)
        every { noticeRepository.delete(notice) } just Runs

        // when
        noticeService.deleteNotice(1L)

        // then
        verify { noticeRepository.delete(notice) }
    }
}
