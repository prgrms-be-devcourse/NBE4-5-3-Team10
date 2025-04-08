//package com.tripfriend.domain.recruit.recruit.dto;
//
//import com.fasterxml.jackson.annotation.JsonProperty;
//import com.tripfriend.domain.member.member.entity.Member;
//import com.tripfriend.domain.place.place.entity.Place;
//import com.tripfriend.domain.recruit.recruit.entity.Recruit;
//import com.tripfriend.domain.recruit.recruit.entity.TravelStyle;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import java.time.LocalDate;
//
//@Data // getter, setter, hashcode, tostring 추가
//@Builder
//@NoArgsConstructor // serialization 시 필요
//@AllArgsConstructor // builder에 필요
//public class RecruitRequestDto {
////    private Long memberId;
////    private List<Apply> applies;
//    private Long placeId;
//    private String title;
//    private String content;
//    @JsonProperty("isClosed")
//    private boolean isClosed;
//    private LocalDate startDate;
//    private LocalDate endDate;
//    private TravelStyle travelStyle;
//    private boolean sameGender;
//    private boolean sameAge;
//    private Integer budget = 0;
//    private Integer groupSize = 2;
//
//    public Recruit toEntity(Member member, Place place){
//        return Recruit.builder()
//                .member(member)
////                .applies(applies)
//                .place(place)
//                .title(title)
//                .content(content)
//                .isClosed(isClosed)
//                .startDate(startDate)
//                .endDate(endDate)
//                .travelStyle(travelStyle)
//                .sameGender(sameGender)
//                .sameAge(sameAge)
//                .budget(budget)
//                .groupSize(groupSize)
//                .build();
//    }
//}
