package com.tripfriend.global.init;

import com.tripfriend.domain.blacklist.entity.Blacklist;
import com.tripfriend.domain.blacklist.repository.BlacklistRepository;
import com.tripfriend.domain.member.member.entity.*;
import com.tripfriend.domain.member.member.repository.MemberRepository;
import com.tripfriend.domain.notice.entity.Notice;
import com.tripfriend.domain.notice.repository.NoticeRepository;
import com.tripfriend.domain.place.place.entity.Category;
import com.tripfriend.domain.place.place.entity.Place;
import com.tripfriend.domain.place.place.repository.PlaceRepository;
import com.tripfriend.domain.qna.entity.Answer;
import com.tripfriend.domain.qna.entity.Question;
import com.tripfriend.domain.qna.repository.AnswerRepository;
import com.tripfriend.domain.qna.repository.QuestionRepository;
import com.tripfriend.domain.recruit.apply.entity.Apply;
import com.tripfriend.domain.recruit.apply.repository.ApplyRepository;
import com.tripfriend.domain.recruit.recruit.entity.Recruit;
import com.tripfriend.domain.recruit.recruit.repository.RecruitRepository;
import com.tripfriend.domain.review.repository.CommentRepository;
import com.tripfriend.domain.review.repository.ReviewRepository;
import com.tripfriend.domain.trip.information.entity.Transportation;
import com.tripfriend.domain.trip.information.entity.TripInformation;
import com.tripfriend.domain.trip.information.repository.TripInformationRepository;
import com.tripfriend.domain.trip.schedule.entity.TripSchedule;
import com.tripfriend.domain.trip.schedule.repository.TripScheduleRepository;
import com.tripfriend.domain.trip.schedule.service.TripScheduleService;
import com.tripfriend.domain.review.entity.Review;
import com.tripfriend.domain.review.entity.Comment;
import com.tripfriend.global.exception.ServiceException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional
public class BaseInitData implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final RecruitRepository recruitRepository;
    private final ApplyRepository applyRepository;
    private final PlaceRepository placeRepository;
    private final TripInformationRepository tripInformationRepository;
    private final TripScheduleRepository tripScheduleRepository;
    private final ReviewRepository reviewRepository;
    private final CommentRepository commentRepository;
    private final NoticeRepository noticeRepository;
    private final BlacklistRepository blacklistRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final PasswordEncoder passwordEncoder;


    @Override
    public void run(String... args) throws Exception {
        initMembers(); // 회원 등록
        initPlace(); // 여행지 등록
        initTripSchedule(); // 여행일정 등록
        initRecruits(); // 동행글 등록
        initApplies(); // 동행댓글 등록
        initReviews(); // 리뷰 등록
        initComments(); // 리뷰댓글 등록
        initNotices(); // 공지 데이터 추가
        initBlacklists(); // 블랙리스트 추가
        initQuestionsAndAnswers(); // 질문 및 답변 추가

    }

    // 회원 등록
    private void initMembers() {
        if (memberRepository.count() == 0) {
            Member user1 = Member.builder()
                    .username("user1")
                    .email("user1@example.com")
                    .password(passwordEncoder.encode("12341234"))
                    .nickname("user1")
                    .gender(Gender.MALE)
                    .ageRange(AgeRange.TWENTIES)
                    .travelStyle(TravelStyle.TOURISM)
                    .aboutMe("hello")
                    .rating(0.0)
                    .authority("USER")
                    .verified(true)
                    .build();
            memberRepository.save(user1);

            Member user2 = Member.builder()
                    .username("user2")
                    .email("user2@example.com")
                    .password(passwordEncoder.encode("12341234"))
                    .nickname("user2")
                    .gender(Gender.FEMALE)
                    .ageRange(AgeRange.THIRTIES)
                    .travelStyle(TravelStyle.SHOPPING)
                    .aboutMe("hello")
                    .rating(0.0)
                    .authority("USER")
                    .verified(true)
                    .build();
            memberRepository.save(user2);

            Member user3 = Member.builder()
                    .username("user3")
                    .email("user3@example.com")
                    .password(passwordEncoder.encode("12341234"))
                    .nickname("user3")
                    .gender(Gender.MALE)
                    .ageRange(AgeRange.FORTIES_PLUS)
                    .travelStyle(TravelStyle.RELAXATION)
                    .aboutMe("hello")
                    .rating(0.0)
                    .authority("USER")
                    .verified(true)
                    .build();
            memberRepository.save(user3);

            Member admin = Member.builder()
                    .username("admin")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("12341234"))
                    .nickname("admin")
                    .gender(Gender.FEMALE)
                    .ageRange(AgeRange.THIRTIES)
                    .travelStyle(TravelStyle.SHOPPING)
                    .aboutMe("hello")
                    .rating(0.0)
                    .authority("ADMIN")
                    .verified(true)
                    .build();
            memberRepository.save(admin);

            System.out.println("회원 테스트 데이터가 등록되었습니다.");
        }else {
            System.out.println("이미 회원 데이터가 존재합니다.");
        }
    }

    // 동행 모집글 등록
    private void initRecruits() {
        if (recruitRepository.count() == 0) {
            List<Member> members = memberRepository.findAll();
            List<Place> places = placeRepository.findAll();

            if (members.isEmpty() || places.isEmpty()) {
                throw new IllegalStateException("회원 또는 장소 데이터가 없습니다.");
            }

            List<Recruit> recruits = List.of(
                    Recruit.builder().member(members.get(0)).place(places.get(0))
                            .title("서울 한강에서 피크닉 함께해요!").content("한강에서 맛있는 음식과 함께 피크닉 즐길 분 모집합니다.")
                            .isClosed(false).startDate(LocalDate.now().plusDays(3)).endDate(LocalDate.now().plusDays(3))
                            .travelStyle(com.tripfriend.domain.recruit.recruit.entity.TravelStyle.RELAXATION)
                            .sameGender(false).sameAge(false).budget(20000).groupSize(4).build(),

                    Recruit.builder().member(members.get(1)).place(places.get(1))
                            .title("부산 바다 여행! 해운대, 광안리 방문 예정").content("바다 여행을 좋아하시는 분과 함께하면 좋겠어요!")
                            .isClosed(false).startDate(LocalDate.now().plusDays(5)).endDate(LocalDate.now().plusDays(8))
                            .travelStyle(com.tripfriend.domain.recruit.recruit.entity.TravelStyle.ADVENTURE)
                            .sameGender(true).sameAge(true).budget(50000).groupSize(3).build(),

                    Recruit.builder().member(members.get(2)).place(places.get(3))
                            .title("강릉 커피 투어 동행 모집").content("강릉의 유명한 커피 명소를 함께 방문할 동행을 찾습니다.")
                            .isClosed(false).startDate(LocalDate.now().plusDays(7)).endDate(LocalDate.now().plusDays(10))
                            .travelStyle(com.tripfriend.domain.recruit.recruit.entity.TravelStyle.GOURMET)
                            .sameGender(false).sameAge(false).budget(30000).groupSize(2).build(),

                    Recruit.builder().member(members.get(1)).place(places.get(5))
                            .title("서울 도심 야경 투어").content("남산, 한강, 롯데타워 전망대 등을 함께 돌면서 야경을 감상해요.")
                            .isClosed(false).startDate(LocalDate.now().plusDays(2)).endDate(LocalDate.now().plusDays(2))
                            .travelStyle(com.tripfriend.domain.recruit.recruit.entity.TravelStyle.RELAXATION)
                            .sameGender(true).sameAge(true).budget(25000).groupSize(5).build(),

                    Recruit.builder().member(members.get(0)).place(places.get(8))
                            .title("제주도 성산일출봉 트레킹").content("이른 아침 일출을 보러 함께 가실 분 구해요!")
                            .isClosed(false).startDate(LocalDate.now().plusDays(10)).endDate(LocalDate.now().plusDays(12))
                            .travelStyle(com.tripfriend.domain.recruit.recruit.entity.TravelStyle.ADVENTURE)
                            .sameGender(false).sameAge(true).budget(60000).groupSize(3).build(),

                    Recruit.builder().member(members.get(2)).place(places.get(10))
                            .title("속초 중앙시장 & 바닷가 투어").content("속초에서 맛집 탐방과 바닷가 드라이브 할 분!")
                            .isClosed(false).startDate(LocalDate.now().plusDays(4)).endDate(LocalDate.now().plusDays(6))
                            .travelStyle(com.tripfriend.domain.recruit.recruit.entity.TravelStyle.GOURMET)
                            .sameGender(true).sameAge(false).budget(40000).groupSize(4).build(),

                    Recruit.builder().member(members.get(1)).place(places.get(12))
                            .title("설악산 단풍 여행 같이 가요!").content("가을 단풍을 보며 힐링할 분 찾습니다.")
                            .isClosed(false).startDate(LocalDate.now().plusDays(15)).endDate(LocalDate.now().plusDays(17))
                            .travelStyle(com.tripfriend.domain.recruit.recruit.entity.TravelStyle.RELAXATION)
                            .sameGender(false).sameAge(false).budget(50000).groupSize(3).build(),

                    Recruit.builder().member(members.get(0)).place(places.get(13))
                            .title("강릉 바다 드라이브 & 맛집 투어").content("바다 드라이브와 유명 맛집 코스를 함께할 분!")
                            .isClosed(false).startDate(LocalDate.now().plusDays(6)).endDate(LocalDate.now().plusDays(9))
                            .travelStyle(com.tripfriend.domain.recruit.recruit.entity.TravelStyle.GOURMET)
                            .sameGender(false).sameAge(false).budget(70000).groupSize(5).build(),

                    Recruit.builder().member(members.get(2)).place(places.get(9))
                            .title("부산 감천마을 & 국제시장 투어").content("부산 여행을 알차게 즐길 분 구해요!")
                            .isClosed(false).startDate(LocalDate.now().plusDays(8)).endDate(LocalDate.now().plusDays(10))
                            .travelStyle(com.tripfriend.domain.recruit.recruit.entity.TravelStyle.SHOPPING)
                            .sameGender(true).sameAge(false).budget(35000).groupSize(3).build(),

                    Recruit.builder().member(members.get(1)).place(places.get(7))
                            .title("경주 문화유산 탐방").content("불국사, 석굴암 등 문화유적지를 돌면서 역사 탐방해요!")
                            .isClosed(false).startDate(LocalDate.now().plusDays(12)).endDate(LocalDate.now().plusDays(15))
                            .travelStyle(com.tripfriend.domain.recruit.recruit.entity.TravelStyle.SIGHTSEEING)
                            .sameGender(false).sameAge(true).budget(45000).groupSize(4).build()
            );

            recruitRepository.saveAll(recruits);
            System.out.println("동행 모집(게시글) 10개가 등록되었습니다.");
        } else {
            System.out.println("이미 동행 모집(게시글) 데이터가 존재합니다.");
        }
    }

    // 동행 요청 댓글 등록
    private void initApplies() {
        if (applyRepository.count() == 0) {
            List<Member> members = memberRepository.findAll();
            List<Recruit> recruits = recruitRepository.findAll();

            if (members.isEmpty() || recruits.isEmpty()) {
                throw new IllegalStateException("회원 또는 동행 모집글 데이터가 없습니다.");
            }

            List<Apply> applies = List.of(
                    Apply.builder().content("한강 피크닉 너무 좋아요! 같이 해요.").member(members.get(2)).recruit(recruits.get(0)).build(),
                    Apply.builder().content("부산 바다 진짜 가고 싶었는데 함께해요!").member(members.get(0)).recruit(recruits.get(1)).build(),
                    Apply.builder().content("강릉 커피 투어 너무 흥미로워요!").member(members.get(1)).recruit(recruits.get(2)).build(),
                    Apply.builder().content("서울 야경 투어 같이 하고 싶어요!").member(members.get(0)).recruit(recruits.get(3)).build(),
                    Apply.builder().content("제주도 일출 보러 가는 거 기대돼요!").member(members.get(2)).recruit(recruits.get(4)).build(),
                    Apply.builder().content("속초 여행 너무 재밌겠어요!").member(members.get(1)).recruit(recruits.get(5)).build(),
                    Apply.builder().content("설악산 단풍 너무 기대돼요!").member(members.get(0)).recruit(recruits.get(6)).build(),
                    Apply.builder().content("강릉 바다 드라이브 코스 완전 좋아요!").member(members.get(2)).recruit(recruits.get(7)).build(),
                    Apply.builder().content("부산 감천마을 너무 가고 싶었어요!").member(members.get(0)).recruit(recruits.get(8)).build(),
                    Apply.builder().content("경주 역사 탐방 너무 흥미로워요!").member(members.get(1)).recruit(recruits.get(9)).build()
            );

            applyRepository.saveAll(applies);
            System.out.println("동행 요청(댓글) 20개가 등록되었습니다.");
        } else {
            System.out.println("이미 동행 요청(댓글) 데이터가 존재합니다.");
        }
    }



    // 여행지 등록
    private void initPlace() {
        if (placeRepository.count() == 0) {
            List<Place> places = List.of(
                    // 서울
                    Place.builder()
                            .cityName("서울")
                            .placeName("경복궁")
                            .description("조선 시대의 대표적인 궁궐로, 한국 전통 건축의 아름다움을 느낄 수 있는 곳입니다.")
                            .category(Category.PLACE) // 관광지
                            .imageUrl("/images/경복궁.jpg")
                            .build(),
                    Place.builder()
                            .cityName("서울")
                            .placeName("신라 호텔")
                            .description("럭셔리한 서비스와 아름다운 전망을 자랑하는 서울의 대표적인 호텔입니다.")
                            .category(Category.STAY) // 숙박 시설
                            .imageUrl("/images/신라호텔.png")
                            .build(),
                    Place.builder()
                            .cityName("서울")
                            .placeName("스타벅스 더종로점")
                            .description("탁 트인 전망과 함께 프리미엄 커피를 즐길 수 있는 카페입니다.")
                            .category(Category.CAFE) // 카페
                            .imageUrl("/images/스타벅스 더종로점.png")
                            .build(),
                    Place.builder()
                            .cityName("서울")
                            .placeName("명동교자")
                            .description("서울에서 가장 유명한 칼국수 맛집 중 하나입니다.")
                            .category(Category.RESTAURANT) // 식당
                            .imageUrl("/images/명동교자.png")
                            .build(),

                    // 부산
                    Place.builder()
                            .cityName("부산")
                            .placeName("해운대 해수욕장")
                            .description("부산을 대표하는 해변으로, 여름철에는 많은 관광객이 찾는 명소입니다.")
                            .category(Category.PLACE) // 관광지
                            .imageUrl("/images/해운대.jpg")
                            .build(),
                    Place.builder()
                            .cityName("부산")
                            .placeName("광안대교 야경")
                            .description("부산의 야경 명소 중 하나로, 광안리 해변에서 아름다운 전망을 볼 수 있습니다.")
                            .category(Category.PLACE) // 관광지
                            .imageUrl("/images/광안대교.jpg")
                            .build(),
                    Place.builder()
                            .cityName("부산")
                            .placeName("기장 연화리 카페거리")
                            .description("바닷가 바로 앞에서 커피를 마실 수 있는 멋진 카페들이 모여 있는 곳입니다.")
                            .category(Category.CAFE) // 카페
                            .imageUrl("/images/기장 연화리 카페거리.jpg")
                            .build(),
                    Place.builder()
                            .cityName("부산")
                            .placeName("초량밀면")
                            .description("부산에서 유명한 밀면 맛집으로, 여름철에 특히 인기가 많습니다.")
                            .category(Category.RESTAURANT) // 식당
                            .imageUrl("/images/초량밀면.jpg")
                            .build(),

                    // 제주도
                    Place.builder()
                            .cityName("제주도")
                            .placeName("성산일출봉")
                            .description("유네스코 세계자연유산으로 지정된 제주도의 대표적인 명소입니다.")
                            .category(Category.PLACE) // 자연 관광지
                            .imageUrl("/images/성산일출봉.jpg")
                            .build(),
                    Place.builder()
                            .cityName("제주도")
                            .placeName("우도")
                            .description("에메랄드빛 바다와 멋진 해안도로가 있는 작은 섬으로, 제주도의 인기 관광지입니다.")
                            .category(Category.PLACE) // 관광지
                            .imageUrl("/images/우도.jpg")
                            .build(),
                    Place.builder()
                            .cityName("제주도")
                            .placeName("제주 흑돼지 거리")
                            .description("제주도에서만 맛볼 수 있는 특색 있는 흑돼지 요리를 즐길 수 있는 곳입니다.")
                            .category(Category.RESTAURANT) // 식당
                            .imageUrl("/images/제주 흑돼지 거리.jpg")
                            .build(),

                    // 강원도 속초
                    Place.builder()
                            .cityName("속초")
                            .placeName("속초 중앙시장")
                            .description("속초에서 가장 유명한 재래시장으로, 다양한 먹거리를 즐길 수 있습니다.")
                            .category(Category.ETC) // 기타 명소
                            .imageUrl("/images/속초중앙시장.jpg")
                            .build(),
                    Place.builder()
                            .cityName("속초")
                            .placeName("설악산 국립공원")
                            .description("대한민국에서 가장 아름다운 산 중 하나로, 사계절 내내 등산객이 찾는 명소입니다.")
                            .category(Category.PLACE) // 자연 관광지
                            .imageUrl("/images/설악산.jpg")
                            .build(),
                    Place.builder()
                            .cityName("속초")
                            .placeName("봉포머구리집")
                            .description("싱싱한 해산물 요리를 맛볼 수 있는 속초의 대표적인 맛집입니다.")
                            .category(Category.RESTAURANT) // 식당
                            .imageUrl("/images/봉포머구리집.png")
                            .build()
            );

            placeRepository.saveAll(places);
            System.out.println("국내 여행지 12개가 등록되었습니다.");
        } else {
            System.out.println("이미 여행지 데이터가 존재합니다.");
        }
    }

    // 여행 일정 생성
    private void initTripSchedule() {
        if (tripScheduleRepository.count() == 0) {
            // 1. 회원 확인
            Member member = memberRepository.findById(1L).orElseThrow(
                    () -> new ServiceException("404-1", "기본 회원이 존재하지 않습니다.")
            );

            // 2. 첫 번째 여행 일정 (서울 힐링 여행)
            TripSchedule tripSchedule1 = TripSchedule.builder()
                    .member(member)
                    .title("서울 힐링 여행")
                    .description("서울에서 고궁과 명소를 둘러보고 맛집 탐방")
                    .startDate(LocalDate.of(2025, 4, 10))
                    .endDate(LocalDate.of(2025, 4, 12))
                    .build();
            tripScheduleRepository.save(tripSchedule1);

            List<TripInformation> tripInformations1 = List.of(
                    createTripInformation(tripSchedule1, 1L, LocalDateTime.of(2025, 4, 10, 9, 0), Transportation.SUBWAY, 3000, "경복궁에서 한복 체험"),
                    createTripInformation(tripSchedule1, 4L, LocalDateTime.of(2025, 4, 10, 12, 0), Transportation.WALK, 0,"명동교자에서 점심"),
                    createTripInformation(tripSchedule1, 3L, LocalDateTime.of(2025, 4, 10, 16, 0), Transportation.BUS, 2000, "스타벅스 더종로점에서 카페 타임")
            );
            tripInformationRepository.saveAll(tripInformations1);
            tripInformations1.forEach(tripSchedule1::addTripInfromation);

            // 3. 두 번째 여행 일정 (부산 바다 여행)
            TripSchedule tripSchedule2 = TripSchedule.builder()
                    .member(member)
                    .title("부산 바다 여행")
                    .description("부산 해운대와 광안대교 야경을 즐기는 일정")
                    .startDate(LocalDate.of(2025, 5, 15))
                    .endDate(LocalDate.of(2025, 5, 17))
                    .build();
            tripScheduleRepository.save(tripSchedule2);

            List<TripInformation> tripInformations2 = List.of(
                    createTripInformation(tripSchedule2, 5L, LocalDateTime.of(2025, 5, 15, 10, 0), Transportation.WALK, 0, "해운대 해수욕장에서 바다 산책"),
                    createTripInformation(tripSchedule2, 6L, LocalDateTime.of(2025, 5, 15, 19, 0), Transportation.TAXI, 10000, "광안대교 야경 감상"),
                    createTripInformation(tripSchedule2, 8L, LocalDateTime.of(2025, 5, 16, 12, 0), Transportation.BUS, 2500, "초량밀면에서 부산 밀면 맛보기")
            );
            tripInformationRepository.saveAll(tripInformations2);
            tripInformations2.forEach(tripSchedule2::addTripInfromation);

            // 4. 세 번째 여행 일정 (제주도 탐방)
            TripSchedule tripSchedule3 = TripSchedule.builder()
                    .member(member)
                    .title("제주도 탐방")
                    .description("제주도의 대표 명소와 맛집을 방문하는 여행")
                    .startDate(LocalDate.of(2025, 6, 20))
                    .endDate(LocalDate.of(2025, 6, 23))
                    .build();
            tripScheduleRepository.save(tripSchedule3);

            List<TripInformation> tripInformations3 = List.of(
                    createTripInformation(tripSchedule3, 9L, LocalDateTime.of(2025, 6, 20, 8, 30), Transportation.CAR, 20000, "성산일출봉에서 일출 보기"),
                    createTripInformation(tripSchedule3, 10L, LocalDateTime.of(2025, 6, 21, 10, 0), Transportation.BUS, 5000, "우도에서 자전거 타기"),
                    createTripInformation(tripSchedule3, 11L, LocalDateTime.of(2025, 6, 21, 18, 30), Transportation.TAXI, 15000, "제주 흑돼지 거리에서 저녁 식사")
            );
            tripInformationRepository.saveAll(tripInformations3);
            tripInformations3.forEach(tripSchedule3::addTripInfromation);

            // 5. 네 번째 여행 일정 (속초 먹거리 여행)
            TripSchedule tripSchedule4 = TripSchedule.builder()
                    .member(member)
                    .title("속초 먹거리 여행")
                    .description("속초에서 재래시장과 해산물 맛집 방문")
                    .startDate(LocalDate.of(2025, 7, 5))
                    .endDate(LocalDate.of(2025, 7, 7))
                    .build();
            tripScheduleRepository.save(tripSchedule4);

            List<TripInformation> tripInformations4 = List.of(
                    createTripInformation(tripSchedule4, 12L, LocalDateTime.of(2025, 7, 5, 11, 0), Transportation.WALK, 0, "속초 중앙시장에서 다양한 먹거리 탐방"),
                    createTripInformation(tripSchedule4, 13L, LocalDateTime.of(2025, 7, 6, 9, 0), Transportation.BUS, 4000, "설악산 국립공원 등산"),
                    createTripInformation(tripSchedule4, 14L, LocalDateTime.of(2025, 7, 6, 18, 0), Transportation.TAXI, 8000, "봉포머구리집에서 신선한 해산물 맛보기")
            );
            tripInformationRepository.saveAll(tripInformations4);
            tripInformations4.forEach(tripSchedule4::addTripInfromation);

            System.out.println("네 개의 여행 일정이 등록되었습니다.");
        } else {
            System.out.println("이미 여행 일정 데이터가 존재합니다.");
        }
    }

    // 여행 정보 생성
    private TripInformation createTripInformation(TripSchedule tripSchedule, Long placeId, LocalDateTime visitTime, Transportation transportation, int cost, String notes) {
        Place place = placeRepository.findById(placeId).orElseThrow(
                () -> new ServiceException("404-2", "해당 장소가 존재하지 않습니다. ID: " + placeId)
        );

        return TripInformation.builder()
                .tripSchedule(tripSchedule)
                .place(place)
                .visitTime(visitTime)
                .duration(2) // 2시간 머무름
                .transportation(transportation) // 교통 수단 설정
                .cost(cost) // 비용 설정
                .notes(notes) // 방문 목적 및 기타 정보 추가
                //.priority(priority) // 우선순위 설정
                .isVisited(false) // 기본값은 방문하지 않음
                .build();
    }

    // 리뷰 데이터 등록
    private void initReviews() {
        if (reviewRepository.count() == 0) {
            Member user = memberRepository.findById(1L).orElseThrow(
                    () -> new ServiceException("404-1", "기본 회원이 존재하지 않습니다.")
            );

            List<Review> reviews = List.of(
                    // 서울 경복궁 리뷰 3개
                    createReview(
                            "경복궁은 정말 아름다워요",
                            "조선 시대의 궁궐 중에서 가장 웅장하고 아름다워요. 한복을 입고 방문하면 무료입장도 가능하고 사진도 예쁘게 나와요!",
                            5.0,
                            user,
                            1L
                    ),
                    createReview(
                            "경복궁 야간 개장 다녀왔어요",
                            "야간에 조명이 들어온 경복궁은 낮과는 또 다른 매력이 있어요. 인파가 적어 여유롭게 관람할 수 있었습니다.",
                            4.6,
                            user,
                            1L
                    ),
                    createReview(
                            "외국인 친구와 함께 방문",
                            "외국에서 온 친구와 함께 경복궁을 방문했는데, 친구가 한국 전통 건축에 감탄했어요. 영어 오디오 가이드도 대여할 수 있어서 좋았습니다.",
                            4.4,
                            user,
                            1L
                    ),

                    // 서울 신라 호텔 리뷰 1개
                    createReview(
                            "신라 호텔에서의 특별한 하룻밤",
                            "서비스가 정말 훌륭했습니다. 객실도 넓고 쾌적했으며, 특히 조식 뷔페가 다양하고 맛있었어요.",
                            4.0,
                            user,
                            2L
                    ),

                    // 서울 스타벅스 더종로점 리뷰 1개
                    createReview(
                            "종로에서 가장 분위기 좋은 카페",
                            "창가 자리에서 보이는 풍경이 정말 좋아요. 커피와 함께 여유로운 시간을 보내기 좋습니다.",
                            4.1,
                            user,
                            3L
                    ),

                    // 서울 명동교자 리뷰 1개
                    createReview(
                            "칼국수 맛집 인정합니다",
                            "국물이 진하고 면이 쫄깃해요. 만두도 함께 주문하시면 정말 맛있습니다!",
                            4.5,
                            user,
                            4L
                    ),

                    // 부산 해운대 해수욕장 리뷰 2개
                    createReview(
                            "해운대 여름 피서 다녀왔어요",
                            "여름 휴가로 해운대에 다녀왔는데 사람이 너무 많았어요. 그래도 바다는 정말 예뻤습니다. 밤에 보는 야경도 최고!",
                            3.9,
                            user,
                            5L
                    ),
                    createReview(
                            "겨울 해운대의 매력",
                            "겨울에 해운대를 방문했는데, 한적하고 조용해서 좋았어요. 겨울바다의 매력을 느낄 수 있었습니다.",
                            3.7,
                            user,
                            5L
                    ),

                    // 부산 광안대교 야경 리뷰 1개
                    createReview(
                            "광안대교의 야경은 환상적이에요",
                            "부산 여행에서 꼭 봐야 할 야경 1위! 광안리 해변에서 보는 광안대교 불빛이 정말 멋있어요.",
                            5.0,
                            user,
                            6L
                    ),

                    // 부산 기장 연화리 카페거리 리뷰 1개
                    createReview(
                            "바다를 보며 커피 한 잔",
                            "기장 연화리 카페거리는 바다가 바로 앞에 보여서 뷰가 정말 좋아요. 카페마다 분위기도 독특해서 골라가는 재미가 있습니다.",
                            4.1,
                            user,
                            7L
                    ),

                    // 부산 초량밀면 리뷰 1개
                    createReview(
                            "부산 오면 꼭 먹어야 하는 밀면",
                            "시원한 국물과 쫄깃한 면발이 일품입니다. 더운 여름에 먹으면 더 맛있어요!",
                            4.8,
                            user,
                            8L
                    ),

                    // 제주도 성산일출봉 리뷰 2개
                    createReview(
                            "성산일출봉 일출 보러 갔어요",
                            "새벽에 일어나기 힘들었지만 일출을 보니 그 모든 수고가 값진 것 같아요. 정상까지 올라가는 길이 조금 가파르니 편한 신발 신고 가세요.",
                            3.9,
                            user,
                            9L
                    ),
                    createReview(
                            "가족 여행으로 성산일출봉",
                            "아이들과 함께 올라갔는데, 아이들도 힘들지 않게 올라갈 수 있었어요. 정상에서 보는 뷰가 아이들에게도 좋은 추억이 되었습니다.",
                            4.6,
                            user,
                            9L
                    ),

                    // 제주도 우도 리뷰 1개
                    createReview(
                            "우도는 작지만 볼거리가 많아요",
                            "자전거를 빌려서 섬 한 바퀴를 돌았는데, 에메랄드빛 바다와 풍경이 정말 아름다웠습니다. 우도 땅콩 아이스크림도 꼭 드세요!",
                            4.8,
                            user,
                            10L
                    ),

                    // 제주도 흑돼지 거리 리뷰 1개
                    createReview(
                            "제주도 흑돼지는 역시 맛있네요",
                            "여러 식당을 가봤는데 모두 맛있었어요. 특히 오겹살과 목살이 두툼하고 맛있었습니다.",
                            5.0,
                            user,
                            11L
                    ),

                    // 속초 중앙시장 리뷰 1개
                    createReview(
                            "속초 중앙시장 맛집 투어",
                            "속초 중앙시장에 있는 모든 맛집을 다 가봤어요. 특히 닭강정과 오징어 순대가 정말 맛있었습니다. 시장 구경도 재밌고 먹거리도 다양해서 좋았어요.",
                            4.7,
                            user,
                            12L
                    ),

                    // 속초 설악산 국립공원 리뷰 1개
                    createReview(
                            "설악산 등산 코스 추천해요",
                            "울산바위 코스로 올라갔는데 경치가 정말 좋았어요. 단풍 시즌에 가면 더 아름다울 것 같아요.",
                            4.4,
                            user,
                            13L
                    ),

                    // 속초 봉포머구리집 리뷰 1개
                    createReview(
                            "신선한 해산물의 맛",
                            "해산물이 정말 신선하고 푸짐해요. 특히 물회와 회덮밥이 일품입니다. 가격도 합리적이었어요.",
                            4.3,
                            user,
                            14L
                    )
            );

            reviewRepository.saveAll(reviews);
            System.out.println("리뷰 테스트 데이터 18개가 등록되었습니다.");
        } else {
            System.out.println("이미 리뷰 데이터가 존재합니다.");
        }
    }

    // 리뷰 생성 헬퍼 메서드
    private Review createReview(String title, String content, double rating, Member member, Long placeId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new ServiceException("404-2", "해당 여행지가 존재하지 않습니다."));

        return new Review(title, content, rating, member, place);
    }


    // 댓글 데이터 등록
    private void initComments() {
        if (commentRepository.count() == 0) {
            Member user = memberRepository.findById(1L).orElseThrow(
                    () -> new ServiceException("404-1", "기본 회원이 존재하지 않습니다.")
            );

            List<Comment> comments = List.of(
                    // 경복궁 댓글 2개
                    new Comment("경복궁 야간개장 시기는 언제인가요?",
                            reviewRepository.getReferenceById(1L), user),
                    new Comment("한복 대여 어디서 하셨나요? 추천해주세요!",
                            reviewRepository.getReferenceById(1L), user),

                    // 신라 호텔 댓글 3개
                    new Comment("객실 뷰는 어땠나요? 조망이 좋은 객실이 있다고 들었어요.",
                            reviewRepository.getReferenceById(2L), user),
                    new Comment("스파 이용은 해보셨나요? 꼭 가보고 싶네요.",
                            reviewRepository.getReferenceById(2L), user),
                    new Comment("저도 여기 좋았습니다.",
                            reviewRepository.getReferenceById(2L), user),

                    // 스타벅스 더종로점 댓글 1개
                    new Comment("카페가 혼잡한 시간대는 언제인가요? 피해서 가고 싶어요.",
                            reviewRepository.getReferenceById(3L), user),

                    // 명동교자 댓글 2개
                    new Comment("칼국수 외에 다른 메뉴도 추천해 주세요!",
                            reviewRepository.getReferenceById(4L), user),
                    new Comment("웨이팅은 얼마나 했나요? 주말에 방문할 예정인데요.",
                            reviewRepository.getReferenceById(4L), user),

                    // 해운대 해수욕장 댓글 2개
                    new Comment("해수욕장 근처에 괜찮은 숙소 추천해주세요.",
                            reviewRepository.getReferenceById(5L), user),
                    new Comment("주변에 맛집은 어디가 있나요?",
                            reviewRepository.getReferenceById(5L), user),

                    // 광안대교 야경 댓글 2개
                    new Comment("야경 사진 찍기 좋은 포인트가 어디인가요?",
                            reviewRepository.getReferenceById(6L), user),
                    new Comment("몇 시쯤 가면 야경이 가장 아름다운가요?",
                            reviewRepository.getReferenceById(6L), user),

                    // 기장 연화리 카페거리 댓글 2개
                    new Comment("주차는 어디에 하면 좋을까요?",
                            reviewRepository.getReferenceById(7L), user),
                    new Comment("가장 뷰가 좋은 카페는 어디인가요?",
                            reviewRepository.getReferenceById(7L), user),

                    // 초량밀면 댓글 2개
                    new Comment("비빔밀면도 맛있나요?",
                            reviewRepository.getReferenceById(8L), user),
                    new Comment("평일에도 사람이 많은가요?",
                            reviewRepository.getReferenceById(8L), user),

                    // 성산일출봉 댓글 2개
                    new Comment("일출을 보려면 몇 시에 도착해야 할까요?",
                            reviewRepository.getReferenceById(9L), user),
                    new Comment("등산로는 어렵지 않나요? 어린이도 올라갈 수 있을까요?",
                            reviewRepository.getReferenceById(9L), user),

                    // 우도 댓글 2개
                    new Comment("우도로 가는 배는 어디서 타나요?",
                            reviewRepository.getReferenceById(10L), user),
                    new Comment("자전거 대여는 얼마인가요?",
                            reviewRepository.getReferenceById(10L), user),

                    // 제주 흑돼지 거리 댓글 2개
                    new Comment("가격대는 어느 정도인가요?",
                            reviewRepository.getReferenceById(11L), user),
                    new Comment("예약은 필수인가요?",
                            reviewRepository.getReferenceById(11L), user),

                    // 속초 중앙시장 댓글 2개
                    new Comment("닭강정 맛집 추천해주세요!",
                            reviewRepository.getReferenceById(12L), user),
                    new Comment("주차장은 어디에 있나요?",
                            reviewRepository.getReferenceById(12L), user),

                    // 설악산 국립공원 댓글 2개
                    new Comment("단풍 시즌은 언제인가요?",
                            reviewRepository.getReferenceById(13L), user),
                    new Comment("초보자도 등산하기 괜찮은 코스가 있을까요?",
                            reviewRepository.getReferenceById(13L), user),

                    // 봉포머구리집 댓글 2개
                    new Comment("회덮밥 가격은 어느 정도인가요?",
                            reviewRepository.getReferenceById(14L), user),
                    new Comment("예약은 받나요? 주말에 방문할 예정인데 사람이 많을까요?",
                            reviewRepository.getReferenceById(14L), user)
            );

            commentRepository.saveAll(comments);
            System.out.println("댓글 테스트 데이터 28개가 등록되었습니다.");
        } else {
            System.out.println("이미 댓글 데이터가 존재합니다.");
        }
    }

    private void initNotices() {
        if (noticeRepository.count() == 0) {
            Member admin = memberRepository.findByUsername("admin").orElseThrow();

            noticeRepository.save(new Notice("공지사항 1", "첫 번째 공지입니다.", admin, LocalDateTime.now()));
            noticeRepository.save(new Notice("공지사항 2", "두 번째 공지입니다.", admin, LocalDateTime.now()));

            System.out.println("공지사항 데이터가 등록되었습니다.");
        } else {
            System.out.println("이미 공지 데이터가 존재합니다.");
        }
    }

    private void initBlacklists() {
        if (blacklistRepository.count() == 0) {
            Member user = memberRepository.findByUsername("user1").orElseThrow();

            blacklistRepository.save(new Blacklist(user, "비정상 행위로 인해 차단됨", LocalDateTime.now()));

            System.out.println("블랙리스트 데이터가 등록되었습니다.");
        } else {
            System.out.println("이미 블랙리스트 데이터가 존재합니다.");
        }


    }

    private void initQuestionsAndAnswers() {
        if (questionRepository.count() == 0) {
            Member user = memberRepository.findByUsername("user1").orElseThrow();
            Member admin = memberRepository.findByUsername("admin").orElseThrow();

            Question question1 = questionRepository.save(new Question(user, "TripFirend란?", "TripFirend에 대해 알고 싶어요.", LocalDateTime.now()));
            Question question2 = questionRepository.save(new Question(user, "바다 여행지 추천해 주세요", "동해 바다로 가고 싶어요 ", LocalDateTime.now()));

            answerRepository.save(new Answer(question1, admin, "좋은 여행 플랫폼입니다 ㅎㅎ.", LocalDateTime.now()));
            answerRepository.save(new Answer(question2, admin, "영덕으로 가시죠.", LocalDateTime.now()));

            System.out.println("질문 & 답변 데이터가 등록되었습니다.");
        } else {
            System.out.println("이미 질문 & 답변 데이터가 존재합니다.");
        }
    }
}