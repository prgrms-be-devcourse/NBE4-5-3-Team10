package com.tripfriend.domain.recruit.recruit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tripfriend.domain.member.member.entity.Gender;
import com.tripfriend.domain.recruit.apply.dto.ApplyResponseDto;
import com.tripfriend.domain.recruit.recruit.entity.Recruit;
import com.tripfriend.domain.recruit.recruit.entity.TravelStyle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecruitDetailResponseDto {
    private Long recruitId;
    //    private Member member;
    private Long memberId;
    private String memberProfileImage;
    private String memberNickname;
    private String genderRestriction;
    private String ageRestriction;
    //    private List<Apply> applies;
//    private Place place;
    private Long placeId;
    private String placeCityName;
    private String placePlaceName;
    private String title;
    private String content;
    @JsonProperty("isClosed")
    private boolean isClosed;
    private LocalDate startDate;
    private LocalDate endDate;
    private String travelStyle;
    private boolean sameGender;
    private boolean sameAge;
    private Integer budget = 0;
    private Integer groupSize = 2;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ApplyResponseDto> applies;

    public RecruitDetailResponseDto(Recruit recruit){
        this.recruitId = recruit.getRecruitId();
//        this.member = recruit.getMember();
        this.memberId = recruit.getMember().getId();
        this.memberNickname = recruit.getMember().getNickname();
        this.memberProfileImage = recruit.getMember().getProfileImage();
//        this.applies = recruit.getApplies();
//        this.place = recruit.getPlace();
        this.placeId = recruit.getPlace().getId();
        this.placeCityName = recruit.getPlace().getCityName();
        this.placePlaceName = recruit.getPlace().getPlaceName();
        this.title = recruit.getTitle();
        this.content = recruit.getContent();
        this.isClosed = recruit.isClosed();
        this.startDate = recruit.getStartDate();
        this.endDate = recruit.getEndDate();
        this.travelStyle = recruit.getTravelStyle().getKoreanName();
        this.sameGender = recruit.isSameGender();
        this.sameAge = recruit.isSameAge();
        this.budget = recruit.getBudget();
        this.groupSize = recruit.getGroupSize();
        this.createdAt = recruit.getCreatedAt();
        this.updatedAt = recruit.getUpdatedAt();
        // 성별 제한 설정
        this.genderRestriction = recruit.isSameGender() && recruit.getMember().getGender() != Gender.UNKNOWN
                ? (recruit.getMember().getGender() == Gender.MALE ? "남자만" : "여자만")
                : (recruit.getMember().getGender() == Gender.UNKNOWN ? "알 수 없음" : "모든 성별");

        // 나이대 제한 설정
        this.ageRestriction = recruit.isSameAge()
                ? switch (recruit.getMember().getAgeRange()) {
            case TEENS -> "10대만";
            case TWENTIES -> "20대만";
            case THIRTIES -> "30대만";
            case FORTIES_PLUS -> "40대 이상만";
            default -> "알 수 없음";
        }
                : "모든 연령대";
    }

    // ✅ 댓글을 포함할 경우에만 사용
    public RecruitDetailResponseDto(Recruit recruit, List<ApplyResponseDto> applies) {
        this(recruit); // 기존 생성자 호출
        this.applies = applies;
    }
}
