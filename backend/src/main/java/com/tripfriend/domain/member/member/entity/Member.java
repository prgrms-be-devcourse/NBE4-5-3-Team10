package com.tripfriend.domain.member.member.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Member {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(name = "username", nullable = false, length = 100, unique = true)
    private String username;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Column(name = "profile_image")
    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "age_range", nullable = false)
    private AgeRange ageRange;

    @Enumerated(EnumType.STRING)
    @Column(name = "travel_style", nullable = false)
    private TravelStyle travelStyle;

    @Lob
    @Column(name = "about_me")
    private String aboutMe;

    @Column(name = "rating", nullable = false)
    private Double rating;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "authority", nullable = false)
    private String authority;

    @Column(name = "verified", nullable = false)
    private boolean verified; // 이메일 인증 여부

    // 소셜 로그인 식별
    @Column(name = "provider")
    private String provider;

    @Column(name = "provider_id")
    private String providerId;

    // soft delete 여부
    @Column(name = "deleted")
    private boolean deleted = false;

    // soft delete 된 날짜
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {

        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // 삭제 취소 가능 여부 확인
    public boolean canBeRestored() {
        // 삭제되지 않았거나 삭제 시간이 기록되지 않은 경우
        if (!deleted || deletedAt == null) {
            return false;
        }

        // 삭제 후 30일 이내인지 확인
        LocalDateTime restoreDeadline = deletedAt.plusDays(30);
        return LocalDateTime.now().isBefore(restoreDeadline);
    }
}
