package com.tripfriend.domain.qna.entity

import com.tripfriend.domain.member.member.entity.Member
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class Answer(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    val question: Question,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    @Column(nullable = false, columnDefinition = "TEXT")
    var content: String,

    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @PrePersist
    fun prePersist() {
        val now = LocalDateTime.now()
        createdAt = now
        updatedAt = now
    }

    constructor(
        question: Question,
        member: Member,
        content: String,
        createdAt: LocalDateTime
    ) : this(
        question = question,
        member = member,
        content = content,
        createdAt = createdAt,
        updatedAt = LocalDateTime.now()
    )

}