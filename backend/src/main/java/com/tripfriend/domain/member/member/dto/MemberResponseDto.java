package com.tripfriend.domain.member.member.dto;

import com.tripfriend.domain.member.member.entity.AgeRange;
import com.tripfriend.domain.member.member.entity.Gender;
import com.tripfriend.domain.member.member.entity.Member;
import com.tripfriend.domain.member.member.entity.TravelStyle;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MemberResponseDto {

    private Long id;
    private String username;
    private String email;
    private String nickname;
    private String profileImage;
    private Gender gender;
    private AgeRange ageRange;
    private TravelStyle travelStyle;
    private String aboutMe;
    private Double rating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String authority;

    public static MemberResponseDto fromEntity(Member member) {

        return new MemberResponseDto(
                member.getId(),
                member.getUsername(),
                member.getEmail(),
                member.getNickname(),
                member.getProfileImage(),
                member.getGender(),
                member.getAgeRange(),
                member.getTravelStyle(),
                member.getAboutMe(),
                member.getRating(),
                member.getCreatedAt(),
                member.getUpdatedAt(),
                member.getAuthority()
        );
    }
}
