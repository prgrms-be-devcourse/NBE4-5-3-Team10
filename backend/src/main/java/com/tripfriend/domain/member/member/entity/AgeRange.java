package com.tripfriend.domain.member.member.entity;

public enum AgeRange {

    TEENS("10대"),
    TWENTIES("20대"),
    THIRTIES("30대"),
    FORTIES_PLUS("40대 이상"),
    UNKNOWN("알 수 없음");

    private final String ageRange;

    AgeRange(String ageRange) {
        this.ageRange = ageRange;
    }

    public String getAgeRange() {
        return ageRange;
    }
}
