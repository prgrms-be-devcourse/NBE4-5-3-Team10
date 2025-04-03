package com.tripfriend.domain.blacklist.entity;

import com.tripfriend.domain.member.member.entity.Member;
import jakarta.persistence.*;
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
public class Blacklist {

    private LocalDateTime createdAt;

    public Blacklist(Member member, String reason, LocalDateTime createdAt) {
        this.member = member;
        this.reason = reason;
        this.createdAt = createdAt;
    }


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    @Column(nullable = false)
    private String reason;

    public void updateReason(String reason) {
        this.reason = reason;
    }
}
