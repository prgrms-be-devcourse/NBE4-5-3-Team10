package com.tripfriend.domain.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReviewRequestDto {

    @NotBlank(message = "제목은 필수 입력 항목입니다.")
    @Size(min = 2, max = 30, message = "제목은 2자 이상 30자 이하로 입력해주세요.")
    private String title;

    @NotBlank(message = "내용은 필수 입력 항목입니다.")
    @Size(min = 10, max = 2000, message = "내용은 10자 이상 2000자 이하로 입력해주세요.")
    private String content;

    @Min(value = 1, message = "평점은 1점 이상이어야 합니다.")
    @Max(value = 5, message = "평점은 5점 이하여야 합니다.")
    private double rating; // int -> double
    private Long placeId;
}