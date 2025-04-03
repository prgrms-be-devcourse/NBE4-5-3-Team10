package com.tripfriend.domain.member.member.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailAuth {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String authCode;

    private LocalDateTime expireAt;

    public EmailAuth(String email, String authCode, LocalDateTime expireAt) {
        this.email = email;
        this.authCode = authCode;
        this.expireAt = expireAt;
    }

    public void updateCode(String newAuthCode, LocalDateTime newExpireAt) {
        this.authCode = newAuthCode;
        this.expireAt = newExpireAt;
    }
}
