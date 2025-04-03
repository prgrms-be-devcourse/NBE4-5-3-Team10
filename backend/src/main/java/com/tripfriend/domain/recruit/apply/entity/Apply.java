package com.tripfriend.domain.recruit.apply.entity;

import com.tripfriend.domain.member.member.entity.Member;
import com.tripfriend.domain.recruit.recruit.entity.Recruit;
import com.tripfriend.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Apply")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Apply extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "apply_id")
    private Long applyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY) // , cascade = CascadeType.REMOVE
    @JoinColumn(name = "recruit_id", nullable = false)
    private Recruit recruit;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;
}
