package com.tripfriend.domain.recruit.apply.service

import com.tripfriend.domain.member.member.entity.AgeRange
import com.tripfriend.domain.member.member.entity.Gender
import com.tripfriend.domain.member.member.entity.Member
import com.tripfriend.domain.member.member.service.AuthService
import com.tripfriend.domain.place.place.entity.Place
import com.tripfriend.domain.recruit.apply.dto.ApplyCreateRequestDto
import com.tripfriend.domain.recruit.apply.entity.Apply
import com.tripfriend.domain.recruit.apply.repository.ApplyRepository
import com.tripfriend.domain.recruit.recruit.entity.Recruit
import com.tripfriend.domain.recruit.recruit.repository.RecruitRepository
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

class ApplyServiceTest {

    private val applyRepository = mockk<ApplyRepository>()
    private val recruitRepository = mockk<RecruitRepository>()
    private val authService = mockk<AuthService>()
    private val applyService = ApplyService(applyRepository, recruitRepository, mockk(), authService)

    @BeforeEach
    fun setUp() {
        clearAllMocks()
    }

    @Test
    @DisplayName("댓글 조회 - 모집글 ID로 조회 성공")
    fun findByRecruitIdSuccess() {
        val member = createTestMember()
        val recruit = createTestRecruit(1L, member, createTestPlace())
        val apply = createTestApply(member, recruit)
        recruit.applies.add(apply)

        every { recruitRepository.findById(1L) } returns Optional.of(recruit)

        val result = applyService.findByRecruitId(1L)

        assertEquals(1, result.size)
        assertEquals("댓글 내용", result[0].content)
    }

    @Test
    @DisplayName("댓글 등록 - 성공")
    fun createApplySuccess() {
        // Given
        val token = "valid_token"
        val place = createTestPlace()
        val recruit = createTestRecruit(1L, mockk(), place)

        val mockMember = mockk<Member>()
        every { mockMember.id } returns 1L
        every { mockMember.profileImage } returns "img.png"
        every { mockMember.nickname } returns "닉네임"

        val requestDto = ApplyCreateRequestDto(content = "댓글 내용")
        val apply = Apply(
            applyId = 1L,
            member = mockMember,
            recruit = recruit,
            content = "댓글 내용"
        ).apply {
            createdAt = LocalDateTime.now()
            updatedAt = LocalDateTime.now()
        }

        every { authService.getLoggedInMember(token) } returns mockMember
        every { recruitRepository.findById(1L) } returns Optional.of(recruit)
        every { applyRepository.save(any()) } returns apply

        // When
        val result = applyService.create(1L, requestDto, token)

        // Then
        assertEquals("댓글 내용", result.content)
        assertEquals(1L, result.memberId)
        assertEquals("닉네임", result.memberNickname)
    }



    @Test
    @DisplayName("댓글 삭제 - 성공 (본인)")
    fun deleteApplySuccess() {
        val token = "token"
        val member = createTestMember()
        val apply = createTestApply(member, createTestRecruit(1L, member, createTestPlace()))

        every { applyRepository.findById(1L) } returns Optional.of(apply)
        every { authService.getLoggedInMember(token) } returns member
        every { applyRepository.deleteById(1L) } returns Unit

        applyService.delete(1L, token)
    }

    // 헬퍼 메서드들
    private fun createTestMember(): Member {
        return Member(
            id = 1L,
            username = "user",
            email = "email@test.com",
            password = "pw",
            nickname = "닉네임",
            profileImage = "img.png",
            gender = Gender.FEMALE,
            ageRange = AgeRange.TWENTIES,
            travelStyle = com.tripfriend.domain.member.member.entity.TravelStyle.RELAXATION,
            aboutMe = null,
            rating = 5.0,
            authority = "USER",
            verified = true
        )
    }

    private fun createTestPlace(): Place {
        return Place().apply {
            id = 1L
            cityName = "서울"
            placeName = "남산"
        }
    }

    private fun createTestRecruit(id: Long, member: Member, place: Place): Recruit {
        return Recruit(
            recruitId = id,
            member = member,
            applies = mutableListOf(),
            place = place,
            title = "제목",
            content = "내용",
            isClosed = false,
            startDate = java.time.LocalDate.now(),
            endDate = java.time.LocalDate.now().plusDays(1),
            travelStyle = com.tripfriend.domain.recruit.recruit.entity.TravelStyle.RELAXATION,
            sameGender = false,
            sameAge = false,
            budget = 100000,
            groupSize = 2
        )
    }

    private fun createTestApply(member: Member, recruit: Recruit): Apply {
        return Apply(
            applyId = 1L,
            member = member,
            recruit = recruit,
            content = "댓글 내용"
        )
    }
}
