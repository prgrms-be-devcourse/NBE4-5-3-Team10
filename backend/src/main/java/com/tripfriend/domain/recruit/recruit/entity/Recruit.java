//package com.tripfriend.domain.recruit.recruit.entity;
//
//import com.fasterxml.jackson.annotation.JsonProperty;
//import com.tripfriend.domain.member.member.entity.Member;
//import com.tripfriend.domain.place.place.entity.Place;
//import com.tripfriend.domain.recruit.apply.entity.Apply;
//import com.tripfriend.domain.recruit.recruit.dto.RecruitRequestDto;
//import com.tripfriend.global.entity.BaseEntity;
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.time.*;
//import java.util.List;
//
//@Entity
//@Table(name = "Recruit")
//@Getter
//// @ToString
//@NoArgsConstructor // jpa가 엔티티 생성할 때 필요로 함
//@AllArgsConstructor // builder에 필요함
//@Builder
//public class Recruit extends BaseEntity {
//    @Id
//    @Column(name = "recruit_id") // 기본키는 원래 not null
//    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto increment
//    private Long recruitId;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "member_id", nullable = false) // 컬럼이름?완, 회원 탈퇴 시 게시글 남아있게 함 -> 탈퇴 기능 없음
//    private Member member;
//
//    @OneToMany(mappedBy = "recruit", cascade = CascadeType.REMOVE) // , orphanRemoval = true
//    @OrderBy("applyId asc")
//    // 글 삭제 시 댓글도 삭제, 리스트에서 제거된 댓글 자동 삭제
//    private List<Apply> applies;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "place_id", nullable = false) // 컬럼이름??완, 장소 선택 필수?완
//    private Place place;
//
//    @Column(name = "title", nullable = false)
//    private String title;
//
//    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
//    private String content;
//
//    @JsonProperty("isClosed")
//    @Column(name = "is_closed", nullable = false)
//    private boolean isClosed = false; // 어차피 null 안 되니까 Boolean 안 씀
//
//    @Column(name = "start_date", nullable = false)
//    private LocalDate startDate;
//
//    @Column(name = "end_date", nullable = false)
//    private LocalDate endDate;
//
//    @Enumerated(EnumType.STRING)
//    @Column(name = "travel_style", nullable = false)
//    private TravelStyle travelStyle;
//
//    @Column(name = "same_gender", nullable = false)
//    private boolean sameGender; // 어차피 null 안 되니까 Boolean 안 씀
//
//    @Column(name = "same_age", nullable = false)
//    private boolean sameAge; // 어차피 null 안 되니까 Boolean 안 씀
//
//    @Column(name = "budget", nullable = false)
//    private Integer budget = 0;
//
//    @Column(name = "group_size", nullable = false)
//    private Integer groupSize = 2;
//
//    public Recruit update(RecruitRequestDto requestDto, Place place){
//        this.place = place;
//        this.title = requestDto.getTitle();
//        this.content = requestDto.getContent();
//        this.isClosed = requestDto.isClosed();
//        this.startDate = requestDto.getStartDate();
//        this.endDate = requestDto.getEndDate();
//        this.travelStyle = requestDto.getTravelStyle();
//        this.sameGender = requestDto.isSameGender();
//        this.sameAge = requestDto.isSameAge();
//        this.budget = requestDto.getBudget();
//        this.groupSize = requestDto.getGroupSize();
//        return this;
//    }
//}
