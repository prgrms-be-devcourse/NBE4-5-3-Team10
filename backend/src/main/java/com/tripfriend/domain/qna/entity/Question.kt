package com.tripfriend.domain.qna.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.tripfriend.domain.member.member.entity.Member
import jakarta.persistence.*
import java.time.LocalDateTime


@Entity
class Question(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @JsonIgnoreProperties("hibernateLazyInitializer", "handler")
    val member: Member,

    @Column(nullable = false, length = 255)
    var title: String,

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

    @OneToMany(mappedBy = "question", cascade = [CascadeType.ALL], orphanRemoval = true)
    var answers: MutableList<Answer> = mutableListOf()

    @PrePersist
    fun prePersist() {
        val now = LocalDateTime.now()
        createdAt = now
        updatedAt = now
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }

    constructor(member: Member, title: String, content: String, createdAt: LocalDateTime) : this(
        member = member,
        title = title,
        content = content,
        createdAt = createdAt,
        updatedAt = LocalDateTime.now()
    )
}

