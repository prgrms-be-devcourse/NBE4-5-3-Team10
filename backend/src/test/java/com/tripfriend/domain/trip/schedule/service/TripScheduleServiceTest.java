package com.tripfriend.domain.trip.schedule.service;

import com.tripfriend.domain.member.member.entity.Member;
import com.tripfriend.domain.member.member.repository.MemberRepository;
import com.tripfriend.domain.member.member.service.MemberService;
import com.tripfriend.domain.trip.information.dto.TripInformationReqDto;
import com.tripfriend.domain.trip.information.dto.TripInformationUpdateReqDto;
import com.tripfriend.domain.trip.information.entity.Transportation;
import com.tripfriend.domain.trip.schedule.dto.*;
import com.tripfriend.global.exception.ServiceException;
import com.tripfriend.global.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class TripScheduleServiceTest {

    @Autowired
    private TripScheduleService tripScheduleService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private Member loginedMember;
    private String token;

    @BeforeEach
    void setup() {
        // test 데이터베이스에 user1이 존재한다고 가정합니다.
        loginedMember = memberRepository.findByUsername("user1").orElseThrow();
        token = jwtUtil.generateAccessToken(loginedMember.getUsername(), loginedMember.getAuthority(), loginedMember.isVerified());
    }

    @Test
    @DisplayName("여행 일정 생성 성공")
    void createScheduleSuccess() {
        TripScheduleReqDto reqDto = new TripScheduleReqDto();
        reqDto.setMemberId(loginedMember.getId());
        reqDto.setTitle("서울 여행");
        reqDto.setDescription("서울 여행 설명");
        reqDto.setCityName("서울");
        reqDto.setStartDate(LocalDate.of(2023, 9, 1));
        reqDto.setEndDate(LocalDate.of(2023, 9, 10));

        // 세부 여행 일정 생성 예시
        TripInformationReqDto infoReq1 = new TripInformationReqDto();
        infoReq1.setPlaceId(1L); // 사용 가능한 장소 ID로 변경 필요
        infoReq1.setVisitTime(LocalDateTime.of(2025, 4, 10, 9, 0));
        infoReq1.setDuration(2);
        infoReq1.setTransportation(Transportation.SUBWAY);
        infoReq1.setCost(3000);
        infoReq1.setNotes("경복궁 방문");

        reqDto.setTripInformations(List.of(infoReq1));

        TripScheduleInfoResDto result = tripScheduleService.createSchedule(reqDto, token);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getTitle()).isEqualTo("서울 여행");
        // 추가 검증 가능 (예: 생성된 세부 여행 정보 등)
    }

    @Test
    @DisplayName("여행 일정 생성 실패 - 빈 도시명")
    void createScheduleFailEmptyCity() {
        TripScheduleReqDto reqDto = new TripScheduleReqDto();
        reqDto.setTitle("빈 도시 테스트");
        reqDto.setDescription("설명");
        reqDto.setCityName("");  // 빈 도시명
        reqDto.setStartDate(LocalDate.of(2023, 9, 1));
        reqDto.setEndDate(LocalDate.of(2023, 9, 10));
        reqDto.setTripInformations(List.of());

        ServiceException ex = assertThrows(ServiceException.class, () -> {
            tripScheduleService.createSchedule(reqDto, token);
        });
        assertThat(ex.getCode()).isEqualTo("400-2");
    }

    @Test
    @DisplayName("여행 일정 생성 실패 - 여행 정보 도시 불일치")
    void createScheduleFailTripInfoCityMismatch() {
        // user1이 서울을 선택했지만, 여행 정보의 장소가 다른 도시인 경우
        TripScheduleReqDto reqDto = new TripScheduleReqDto();
        reqDto.setMemberId(loginedMember.getId());
        reqDto.setTitle("서울 여행");
        reqDto.setDescription("서울 여행 설명");
        reqDto.setCityName("서울");  // 선택 도시: 서울
        reqDto.setStartDate(LocalDate.of(2023, 9, 1));
        reqDto.setEndDate(LocalDate.of(2023, 9, 10));

        // 여행 정보 예시: 해당 장소의 도시가 "부산" 등 서울과 다른 경우
        TripInformationReqDto infoReq = new TripInformationReqDto();
        infoReq.setPlaceId(6L); // 6L은 부산의 도시 장소Id
        infoReq.setVisitTime(LocalDateTime.of(2025, 4, 10, 9, 0));
        infoReq.setDuration(2);
        infoReq.setTransportation(Transportation.SUBWAY);
        infoReq.setCost(3000);
        infoReq.setNotes("부산 명소 방문");

        reqDto.setTripInformations(List.of(infoReq));

        ServiceException ex = assertThrows(ServiceException.class, () -> {
            tripScheduleService.createSchedule(reqDto, token);
        });
        assertThat(ex.getCode()).isEqualTo("400-1");
    }

    @Test
    @DisplayName("여행 일정 삭제 성공")
    void deleteScheduleSuccess() {

        Long scheduleId = 1L;

        // 삭제 수행
        tripScheduleService.deleteSchedule(scheduleId, token);

        // 삭제 후 조회 시 ServiceException 또는 null 반환 등의 방식으로 검증할 수 있습니다.
    }

    @Test
    @DisplayName("여행 일정 삭제 실패 - 소유자가 아님")
    void deleteScheduleFailNotOwner() {

        // user1의 여행 일정 ID
        Long scheduleId = 1L;

        // user2의 토큰을 사용해 삭제 시도
        Member otherMember = memberRepository.findByUsername("user2").orElseThrow();
        String otherToken = jwtUtil.generateAccessToken(otherMember.getUsername(), otherMember.getAuthority(), otherMember.isVerified());

        ServiceException ex = assertThrows(ServiceException.class, () -> {
            tripScheduleService.deleteSchedule(scheduleId, otherToken);
        });
        assertThat(ex.getCode()).isEqualTo("403-1");
    }

    @Test
    @DisplayName("여행 일정 수정 성공")
    void updateScheduleSuccess() {

        // 여행 스케줄 id
        Long scheduleId = 1L;

        // 세부 스케줄 id
        Long tripInfoId = 1L;

        // 업데이트 DTO 생성
        TripScheduleUpdateReqDto updateSchedule = new TripScheduleUpdateReqDto();
        updateSchedule.setTitle("수정된 제목");
        updateSchedule.setDescription("수정된 설명");

        TripInformationUpdateReqDto updateTripInfo = new TripInformationUpdateReqDto();
        updateTripInfo.setTripInformationId(tripInfoId);
        updateTripInfo.setCost(5000); // 수정된 비용
        updateTripInfo.setNotes("수정된 노트"); // 수정된 노트
        updateTripInfo.setDuration(1);// 수정된 일정

        // 업데이트 요청 DTO 생성
        TripUpdateReqDto updateReq = new TripUpdateReqDto();
        updateReq.setTripScheduleId(scheduleId);
        updateReq.setScheduleUpdate(updateSchedule);
        updateReq.setTripInformationUpdates(List.of(updateTripInfo));

        // 3. 수정 메서드 호출
        TripUpdateResDto updateRes = tripScheduleService.updateTrip(updateReq, token);

        // 4. 수정 결과 검증
        assertThat(updateRes.getUpdatedSchedule().getTitle()).isEqualTo("수정된 제목");
        assertThat(updateRes.getUpdatedSchedule().getDescription()).isEqualTo("수정된 설명");
        assertThat(updateRes.getUpdatedTripInformations().get(0).getCost()).isEqualTo(5000);
        assertThat(updateRes.getUpdatedTripInformations().get(0).getNotes()).isEqualTo("수정된 노트");
        assertThat(updateRes.getUpdatedTripInformations().get(0).getDuration()).isEqualTo(1);
    }

    @Test
    @DisplayName("여행 일정 수정 실패 - 일정 생성자가 아님")
    void updateTripFailNotOwner() {

        // 여행 스케줄 id
        Long scheduleId = 1L;
        // 세부 스케줄 id
        Long tripInfoId = 1L;

        // 업데이트 DTO 생성
        TripScheduleUpdateReqDto updateSchedule = new TripScheduleUpdateReqDto();
        updateSchedule.setTitle("수정된 제목");
        updateSchedule.setDescription("수정된 설명");

        TripInformationUpdateReqDto updateTripInfo = new TripInformationUpdateReqDto();
        updateTripInfo.setTripInformationId(tripInfoId);
        updateTripInfo.setCost(5000); // 수정된 비용
        updateTripInfo.setNotes("수정된 노트"); // 수정된 노트
        updateTripInfo.setDuration(1);

        // 업데이트 요청 DTO 생성
        TripUpdateReqDto updateReq = new TripUpdateReqDto();
        updateReq.setTripScheduleId(scheduleId);
        updateReq.setScheduleUpdate(updateSchedule);
        updateReq.setTripInformationUpdates(List.of(updateTripInfo));

        // user2의 토큰 생성 (user1이 생성한 일정을 수정할 수 없음)
        Member otherMember = memberRepository.findByUsername("user2").orElseThrow();
        String otherToken = jwtUtil.generateAccessToken(otherMember.getUsername(), otherMember.getAuthority(), otherMember.isVerified());

        ServiceException ex = assertThrows(ServiceException.class, () -> {
            tripScheduleService.updateTrip(updateReq, otherToken);
        });
        // 일정 생성자가 아니므로 "403-1" 에러 코드가 발생해야 함
        assertThat(ex.getCode()).isEqualTo("403-1");
    }

    @Test
    @DisplayName("내 여행 일정 조회 성공")
    void getSchedulesByCreatorSuccess() {
        List<TripScheduleResDto> schedules = tripScheduleService.getSchedulesByCreator(token);
        assertThat(schedules).isNotEmpty();
    }

    @Test
    @DisplayName("여행 일정 세부 정보 조회 성공")
    void getTripInfoSuccess() {
        Long scheduleId = 1L;
        List<?> tripInfoList = tripScheduleService.getTripInfo(token, scheduleId);
        assertThat(tripInfoList).isNotEmpty();
    }

    @Test
    @DisplayName("특정 회원의 여행 일정 조회 실패 - 회원 미존재")
    void getSchedulesByMemberIdFailNotFound() {
        // 존재하지 않는 회원 ID 사용 (예: 9999L)
        Long nonExistentMemberId = 9999L;

        ServiceException ex = assertThrows(ServiceException.class, () -> {
            tripScheduleService.getSchedulesByMemberId(nonExistentMemberId);
        });
        assertThat(ex.getCode()).isEqualTo("404-1");
    }



}

