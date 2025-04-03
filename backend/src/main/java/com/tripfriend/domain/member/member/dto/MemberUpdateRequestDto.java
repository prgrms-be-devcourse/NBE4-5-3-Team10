package com.tripfriend.domain.member.member.dto;

import com.tripfriend.domain.member.member.entity.AgeRange;
import com.tripfriend.domain.member.member.entity.Gender;
import com.tripfriend.domain.member.member.entity.TravelStyle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberUpdateRequestDto {

    private String nickname;
    private String email;
    private String password;
    private String profileImage;
    private Gender gender;
    private AgeRange ageRange;
    private TravelStyle travelStyle;
    private String aboutMe;
}
