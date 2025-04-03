package com.tripfriend.domain.member.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenInfoDto {
    private String username;
    private String authority;
    private boolean verified;
}
