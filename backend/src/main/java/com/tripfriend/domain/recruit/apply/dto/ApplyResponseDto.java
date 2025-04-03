package com.tripfriend.domain.recruit.apply.dto;

import com.tripfriend.domain.recruit.apply.entity.Apply;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplyResponseDto {
    private Long applyId;
    //    private Member member;
    private Long memberId;
    private String memberProfileImage;
    private String memberNickname;
    //    private Recruit recruit;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ApplyResponseDto(Apply apply) {
        this.applyId = apply.getApplyId();
//        this.member = apply.getMember();
        this.memberId = apply.getMember().getId();
        this.memberProfileImage = apply.getMember().getProfileImage();
        this.memberNickname = apply.getMember().getNickname();
//        this.recruit = apply.getRecruit();
        this.content = apply.getContent();
        this.createdAt = apply.getCreatedAt();
        this.updatedAt = apply.getUpdatedAt();
    }
}
