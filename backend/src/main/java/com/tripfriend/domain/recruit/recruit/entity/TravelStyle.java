package com.tripfriend.domain.recruit.recruit.entity;

public enum TravelStyle {
    SIGHTSEEING("관광"), // 관광
    RELAXATION("휴양"), // 휴양
    ADVENTURE("액티비티"), // 액티비티
    GOURMET("미식"), // 미식
    SHOPPING("쇼핑");// 쇼핑

    private final String koreanName; // 🔹 한글 값 저장

    TravelStyle(String koreanName) {
        this.koreanName = koreanName;
    }

    public String getKoreanName() {
        return koreanName;
    }
}
