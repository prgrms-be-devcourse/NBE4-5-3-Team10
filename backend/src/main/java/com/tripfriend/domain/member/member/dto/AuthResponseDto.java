package com.tripfriend.domain.member.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponseDto {

    private String accessToken;
    private boolean isDeletedAccount;

    public AuthResponseDto(String accessToken) {
        this.accessToken = accessToken;
        this.isDeletedAccount = false;
    }
}