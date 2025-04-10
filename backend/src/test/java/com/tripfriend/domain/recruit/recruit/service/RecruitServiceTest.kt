package com.tripfriend.domain.recruit.recruit.service

import com.tripfriend.domain.member.member.entity.AgeRange
import com.tripfriend.domain.member.member.entity.Gender
import com.tripfriend.domain.member.member.entity.Member
import com.tripfriend.domain.member.member.entity.TravelStyle
import com.tripfriend.domain.member.member.service.AuthService
import com.tripfriend.domain.place.place.entity.Place
import com.tripfriend.domain.place.place.repository.PlaceRepository
import com.tripfriend.domain.recruit.apply.entity.Apply
import com.tripfriend.domain.recruit.recruit.dto.RecruitRequestDto
import com.tripfriend.domain.recruit.recruit.entity.Recruit
import com.tripfriend.domain.recruit.recruit.repository.RecruitRepository
import com.tripfriend.global.exception.ServiceException
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.util.*

class RecruitServiceTest {
    private val recruitRepository = mockk<RecruitRepository>()
    private val placeRepository = mockk<PlaceRepository>()
    private val authService = mockk<AuthService>()
    private val recruitService = RecruitService(recruitRepository, placeRepository, authService)

    @BeforeEach
    fun setUp() {
        clearAllMocks()
    }

    @Test
    @DisplayName("모집글 단건 조회 - 성공")
    fun findByIdSuccess() {

        // Given
        val recruitId = 1L
        val member = createTestMember()
        val place = createTestPlace()
        val apply = createTestApply(member)
        val recruit = createTestRecruit(recruitId, member, place, listOf(apply))

        every { recruitRepository.findById(recruitId) } returns Optional.of(recruit)

        // When
        val result = recruitService.findById(recruitId)

        // Then
        assertEquals(recruitId, result.recruitId)
        assertEquals("테스트유저", result.memberNickname)
        assertEquals("서울", result.placeCityName)
        assertEquals("남산타워", result.placePlaceName)
        assertEquals("파리 여행 같이 가요!", result.title)
        assertEquals("참여하고 싶어요", result.applies[0].content)
    }


    @Test
    @DisplayName("모집글 단건 조회 - 실패 (존재하지 않음)")
    fun findByIdFail() {
        // Given
        val recruitId = 999L
        every { recruitRepository.findById(recruitId) } returns Optional.empty()

        // When & Then
        val exception = assertThrows<ServiceException> {
            recruitService.findById(recruitId)
        }

        assertEquals("404-3", exception.code)
        assertEquals("해당 모집글이 존재하지 않습니다.", exception.message)
    }

    @Test
    @DisplayName("모집글 생성 - 성공")
    fun createRecruitSuccess() {
        // Given
        val token = "valid_token"
        val member = createTestMember()
        val place = createTestPlace()

        val requestDto = mockk<RecruitRequestDto>()  // DTO는 mock으로 처리
        val recruit = createTestRecruit(
            recruitId = 1L,
            member = member,
            place = place
        )

        // 🔹 내부 호출 모킹 설정
        every { authService.getLoggedInMember(token) } returns member                    // 로그인된 멤버 반환
        every { requestDto.placeId } returns place.id                                    // DTO 내부 placeId
        every { placeRepository.findById(place.id!!) } returns Optional.of(place)        // 장소 찾기
        every { requestDto.toEntity(member, place) } returns recruit                     // DTO -> Recruit 변환
        every { recruitRepository.save(recruit) } returns recruit                        // 저장된 Recruit 리턴

        // When
        val result = recruitService.create(requestDto, token)

        // Then
        assertNotNull(result) // 결과가 null이 아닌지만 검증 (추가적으로 값 비교 가능)
    }

    @Test
    @DisplayName("모집글 생성 - 실패 (로그인하지 않은 경우)")
    fun createRecruitFailDueToNoLogin() {
        // Given
        val token = "invalid_token" // 로그인 정보가 없는 토큰
        val requestDto = mockk<RecruitRequestDto>()

        // 🔹 로그인된 멤버가 없도록 설정 (null 반환)
        every { authService.getLoggedInMember(token) } throws ServiceException("401-1", "로그인이 필요합니다.")

        // When & Then
        val exception = assertThrows<ServiceException> {
            recruitService.create(requestDto, token)
        }

        assertEquals("401-1", exception.code)
        assertEquals("로그인이 필요합니다.", exception.message)
    }


    @Test
    @DisplayName("모집글 생성 - 실패 (존재하지 않는 장소)")
    fun createRecruitFailDueToMissingPlace() {
        // Given
        val token = "valid_token"
        val member = createTestMember()
        val invalidPlaceId = 999L

        val requestDto = mockk<RecruitRequestDto>()

        // ✅ 요청 DTO에서 잘못된 placeId 반환하도록 설정
        every { requestDto.placeId } returns invalidPlaceId

        // 🔹 멤버는 정상 반환되지만, 장소는 Optional.empty()를 반환하게 설정
        every { authService.getLoggedInMember(token) } returns member
        every { placeRepository.findById(invalidPlaceId) } returns Optional.empty()

        // When & Then
        val exception = assertThrows<ServiceException> {
            recruitService.create(requestDto, token)
        }

        assertEquals("404-2", exception.code)
        assertEquals("해당 장소가 존재하지 않습니다.", exception.message)
    }

    @Test
    @DisplayName("모집글 전체 조회 - 성공")
    fun findAllSuccess() {
        // Given
        val member = createTestMember()                                // 테스트 멤버 생성
        val place = createTestPlace()                                  // 테스트 장소 생성

        val recruit1 = createTestRecruit(1L, member, place)            // 모집글 1
        val recruit2 = createTestRecruit(2L, member, place)            // 모집글 2

        val recruitList = listOf(recruit2, recruit1)                   // 최신순 정렬 가정

        every { recruitRepository.findAllByOrderByCreatedAtDesc() } returns recruitList

        // When
        val result = recruitService.findAll()

        // Then
        assertEquals(2, result.size)
        assertEquals(recruit2.recruitId, result[0].recruitId)
        assertEquals(recruit1.recruitId, result[1].recruitId)
    }

    @Test
    @DisplayName("최근 모집글 3개 조회 - 성공")
    fun findRecent3Success() {
        // Given
        val member = createTestMember()
        val place = createTestPlace()

        val recruit1 = createTestRecruit(1L, member, place)
        val recruit2 = createTestRecruit(2L, member, place)
        val recruit3 = createTestRecruit(3L, member, place)

        val recentRecruits = listOf(recruit3, recruit2, recruit1) // 최신순

        every { recruitRepository.findTop3ByOrderByCreatedAtDesc() } returns recentRecruits

        // When
        val result = recruitService.findRecent3()

        // Then
        assertEquals(3, result.size)
        assertEquals(recruit3.recruitId, result[0].recruitId)
        assertEquals(recruit2.recruitId, result[1].recruitId)
        assertEquals(recruit1.recruitId, result[2].recruitId)
    }

    @Test
    @DisplayName("모집글 검색 - 제목/내용 키워드로 검색 성공")
    fun searchRecruitsSuccess() {
        // Given
        val keyword = "파리"
        val member = createTestMember()
        val place = createTestPlace()

        val recruit1 = createTestRecruit(1L, member, place).apply {
            title = "파리 여행 같이 가요!"
            content = "정말 가고 싶은 곳이에요"
        }

        val recruit2 = createTestRecruit(2L, member, place).apply {
            title = "파리 여행 메이트 구해요"
            content = "파리 일정으로 함께할 분!"
        }

        val matchedRecruits = listOf(recruit1, recruit2)

        every { recruitRepository.searchByTitleOrContent(keyword) } returns matchedRecruits

        // When
        val result = recruitService.searchRecruits(keyword)

        // Then
        assertEquals(2, result.size)
        assert(result[0].title.contains("파리"))
        assert(result[1].title.contains("파리"))
    }

    @Test
    @DisplayName("모집글 검색 - isClosed 필터로 검색 성공")
    fun searchByIsClosedSuccess() {
        // Given
        val member = createTestMember()
        val place = createTestPlace()

        val recruit1 = createTestRecruit(1L, member, place).apply { isClosed = true }
        val recruit2 = createTestRecruit(2L, member, place).apply { isClosed = true }

        val closedRecruits = listOf(recruit1, recruit2)

        every { recruitRepository.findByIsClosed(true) } returns closedRecruits

        // When
        val result = recruitService.searchByIsClosed(true)

        // Then
        assertEquals(2, result.size)
        assertTrue(result.all { it.isClosed }) // 모든 결과가 true여야 함
    }

    @Test
    @DisplayName("모집글 검색 - 복합 필터 및 정렬 조건 적용 성공")
    fun searchAndFilterSuccess() {
        // Given
        val token = "valid_token"
        val member = createTestMember()
        val place = createTestPlace()
        val recruit = createTestRecruit(1L, member, place)

        val filteredList = listOf(recruit)

        every { authService.getLoggedInMember(token) } returns member
        every {
            recruitRepository.searchFilterSort(
                keyword = Optional.of("여행"),
                placeCityName = Optional.of("서울"),
                isClosed = Optional.of(false),
                startDate = Optional.of(LocalDate.now()),
                endDate = Optional.of(LocalDate.now().plusDays(3)),
                travelStyle = Optional.of("RELAXATION"),
                sameGender = Optional.of(true),
                sameAge = Optional.of(false),
                minBudget = Optional.of(10000),
                maxBudget = Optional.of(500000),
                minGroupSize = Optional.of(1),
                maxGroupSize = Optional.of(5),
                sortBy = Optional.of("budget_desc"),
                userGender = member.gender,
                userAgeRange = member.ageRange
            )
        } returns filteredList

        // When
        val result = recruitService.searchAndFilter(
            keyword = Optional.of("여행"),
            placeCityName = Optional.of("서울"),
            isClosed = Optional.of(false),
            startDate = Optional.of(LocalDate.now()),
            endDate = Optional.of(LocalDate.now().plusDays(3)),
            travelStyle = Optional.of("RELAXATION"),
            sameGender = Optional.of(true),
            sameAge = Optional.of(false),
            minBudget = Optional.of(10000),
            maxBudget = Optional.of(500000),
            minGroupSize = Optional.of(1),
            maxGroupSize = Optional.of(5),
            sortBy = Optional.of("budget_desc"),
            token = token
        )

        // Then
        assertEquals(1, result.size)
        assertEquals(recruit.recruitId, result[0].recruitId)
    }

    @Test
    @DisplayName("모집글 수정 - 성공 (작성자 본인)")
    fun updateRecruitSuccess() {
        // Given
        val token = "valid_token"
        val member = createTestMember()
        val place = createTestPlace()
        val recruit = createTestRecruit(1L, member, place)
        val requestDto = createTestRecruitRequestDto(place)

        every { recruitRepository.findById(1L) } returns Optional.of(recruit)
        every { placeRepository.findById(place.id!!) } returns Optional.of(place)
        every { authService.getLoggedInMember(token) } returns member

        // When
        val result = recruitService.update(1L, requestDto, token)

        // Then
        assertEquals(recruit.recruitId, result.recruitId)
        assertEquals(member.nickname, result.memberNickname)
        assertEquals("수정된 제목", result.title)
    }

    @Test
    @DisplayName("모집글 수정 - 실패 (모집글이 존재하지 않는 경우)")
    fun updateRecruitFailDueToMissingRecruit() {
        // Given
        val token = "valid_token"
        val member = createTestMember()
        val place = createTestPlace()

        val requestDto = createTestRecruitRequestDto(place)

        every { recruitRepository.findById(1L) } returns Optional.empty()

        // When & Then
        val exception = assertThrows<ServiceException> {
            recruitService.update(1L, requestDto, token)
        }

        assertEquals("404-3", exception.code)
        assertEquals("해당 모집글이 존재하지 않습니다.", exception.message)
    }

    @Test
    @DisplayName("모집글 수정 - 실패 (장소가 존재하지 않는 경우)")
    fun updateRecruitFailDueToMissingPlace() {
        // Given
        val token = "valid_token"
        val member = createTestMember()
        val place = createTestPlace()
        val recruit = createTestRecruit(1L, member, place)
        val requestDto = createTestRecruitRequestDto(place)

        every { recruitRepository.findById(1L) } returns Optional.of(recruit)
        every { placeRepository.findById(place.id!!) } returns Optional.empty() // ❌ 존재하지 않는 장소
        every { authService.getLoggedInMember(token) } returns member

        // When & Then
        val exception = assertThrows<ServiceException> {
            recruitService.update(1L, requestDto, token)
        }

        assertEquals("404-2", exception.code)
        assertEquals("해당 장소가 존재하지 않습니다.", exception.message)
    }

    @Test
    @DisplayName("모집글 수정 - 실패 (로그인하지 않은 경우)")
    fun updateRecruitFailDueToNotLoggedIn() {
        // Given
        val invalidToken = "invalid_token"
        val place = createTestPlace()
        val member = createTestMember()
        val recruit = createTestRecruit(1L, member, place)
        val requestDto = createTestRecruitRequestDto(place)

        every { recruitRepository.findById(1L) } returns Optional.of(recruit)
        every { placeRepository.findById(place.id!!) } returns Optional.of(place)
        every { authService.getLoggedInMember(invalidToken) } throws ServiceException("401-1", "로그인이 필요합니다.") // 실제로 null을 리턴하지는 않고 예외 던진다.

        // When & Then
        val exception = assertThrows<ServiceException> {
            recruitService.update(1L, requestDto, invalidToken)
        }

        assertEquals("401-1", exception.code)
        assertEquals("로그인이 필요합니다.", exception.message)
    }


    @Test
    @DisplayName("모집글 수정 - 실패 (작성자가 아니고 관리자도 아닌 경우)")
    fun updateRecruitFailDueToNoPermission() {
        // Given
        val token = "valid_token"
        val writer = createTestMember() // 모집글 작성자
        val stranger = createTestMember().copy(id = 999L, nickname = "낯선이", authority = "USER") // 다른 사람
        val place = createTestPlace()
        val recruit = createTestRecruit(1L, writer, place)
        val requestDto = createTestRecruitRequestDto(place)

        every { recruitRepository.findById(1L) } returns Optional.of(recruit)
        every { placeRepository.findById(place.id!!) } returns Optional.of(place)
        every { authService.getLoggedInMember(token) } returns stranger

        // When & Then
        val exception = assertThrows<ServiceException> {
            recruitService.update(1L, requestDto, token)
        }

        assertEquals("403-2", exception.code)
        assertEquals("관리자가 아니라면 본인이 등록한 동행 모집글만 수정할 수 있습니다.", exception.message)
    }

    @Test
    @DisplayName("모집글 삭제 - 성공 (작성자 본인)")
    fun deleteRecruitSuccessAsOwner() {
        // Given
        val token = "valid_token"
        val member = createTestMember()
        val place = createTestPlace()
        val recruit = createTestRecruit(1L, member, place)

        every { recruitRepository.findById(1L) } returns Optional.of(recruit)
        every { authService.getLoggedInMember(token) } returns member
        every { recruitRepository.deleteById(1L) } returns Unit // void 함수라는 뜻

        // When
        recruitService.delete(1L, token)

        // Then: 예외 안 나면 성공!
    }

    @Test
    @DisplayName("모집글 삭제 - 실패 (모집글이 존재하지 않는 경우)")
    fun deleteRecruitFailDueToMissingRecruit() {
        // Given
        val token = "valid_token"
        val member = createTestMember()

        every { recruitRepository.findById(1L) } returns Optional.empty()
        every { authService.getLoggedInMember(token) } returns member // 실제로 호출 안 됨 (short-circuit)

        // When & Then
        val exception = assertThrows<ServiceException> {
            recruitService.delete(1L, token)
        }

        assertEquals("404-3", exception.code)
        assertEquals("해당 모집글이 존재하지 않습니다.", exception.message)
    }

    @Test
    @DisplayName("모집글 삭제 - 실패 (로그인하지 않은 경우)")
    fun deleteRecruitFailDueToNotLoggedIn() {
        // Given
        val invalidToken = "invalid_token"
        val member = createTestMember()
        val place = createTestPlace()
        val recruit = createTestRecruit(1L, member, place)

        // mock 설정
        every { recruitRepository.findById(1L) } returns Optional.of(recruit)
        every { authService.getLoggedInMember(invalidToken) } throws ServiceException("401-1", "로그인이 필요합니다.") // ❗ 로그인 실패

        // When & Then
        val exception = assertThrows<ServiceException> {
            recruitService.delete(1L, invalidToken)
        }

        assertEquals("401-1", exception.code)
        assertEquals("로그인이 필요합니다.", exception.message)
    }


    @Test
    @DisplayName("모집글 삭제 - 실패 (작성자가 아니고 관리자도 아님)")
    fun deleteRecruitFailDueToNoPermission() {
        // Given
        val token = "valid_token"
        val writer = createTestMember() // 글 작성자
        val stranger = createTestMember().copy(id = 999L, authority = "USER", nickname = "낯선이")
        val place = createTestPlace()
        val recruit = createTestRecruit(1L, writer, place)

        every { recruitRepository.findById(1L) } returns Optional.of(recruit)
        every { authService.getLoggedInMember(token) } returns stranger

        // When & Then
        val exception = assertThrows<ServiceException> {
            recruitService.delete(1L, token)
        }

        assertEquals("403-2", exception.code)
        assertEquals("관리자가 아니라면 본인이 등록한 동행 모집글만 삭제할 수 있습니다.", exception.message)
    }


    // 공통 테스트 데이터 생성 함수들
    private fun createTestMember(): Member {
        return Member(
            id = 1L,
            username = "testuser",
            email = "test@example.com",
            password = "password123",
            nickname = "테스트유저",
            profileImage = "profile.jpg",
            gender = Gender.FEMALE,
            ageRange = AgeRange.TWENTIES,
            travelStyle = TravelStyle.RELAXATION,
            aboutMe = "안녕하세요",
            rating = 4.5,
            authority = "USER",
            verified = true
        )
    }

    private fun createTestPlace(): Place {
        return Place().apply {
            id = 100L
            cityName = "서울"
            placeName = "남산타워"
        }
    }

    private fun createTestApply(member: Member): Apply {
        return Apply(
            applyId = 999L,
            member = member,
            recruit = mockk(),
            content = "참여하고 싶어요"
        )
    }

    private fun createTestRecruitRequestDto(place: Place): RecruitRequestDto {
        return RecruitRequestDto(
            title = "수정된 제목",
            content = "수정된 내용",
            isClosed = false,
            startDate = LocalDate.now().plusDays(5),
            endDate = LocalDate.now().plusDays(10),
            travelStyle = com.tripfriend.domain.recruit.recruit.entity.TravelStyle.RELAXATION,
            sameGender = true,
            sameAge = true,
            budget = 500000,
            groupSize = 4,
            placeId = place.id!!
        )
    }

    private fun createTestRecruit(
        recruitId: Long = 1L,
        member: Member,
        place: Place,
        applies: List<Apply> = emptyList()
    ): Recruit {
        return Recruit(
            recruitId = recruitId,
            member = member,
            applies = applies.toMutableList(),
            place = place,
            title = "파리 여행 같이 가요!",
            content = "일정 같이 짜요~",
            isClosed = false,
            startDate = LocalDate.now(),
            endDate = LocalDate.now().plusDays(3),
            travelStyle = com.tripfriend.domain.recruit.recruit.entity.TravelStyle.RELAXATION,
            sameGender = false,
            sameAge = false,
            budget = 300000,
            groupSize = 3
        )
    }

}
