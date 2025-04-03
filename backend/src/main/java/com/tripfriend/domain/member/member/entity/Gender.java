package com.tripfriend.domain.member.member.entity;

public enum Gender {

    MALE("남성"),
    FEMALE("여성"),
    UNKNOWN("알 수 없음");;

    private final String gender;

    private Gender(String gender) {
        this.gender = gender;
    }

    public String getGender() {
        return gender;
    }
}
