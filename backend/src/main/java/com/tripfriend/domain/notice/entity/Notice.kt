package com.tripfriend.domain.notice.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class Notice(

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

    // 생성 시 자동 시간 세팅
    @PrePersist
    fun prePersist() {
        val now = LocalDateTime.now()
        createdAt = now
        updatedAt = now
    }

    // 업데이트 시 시간 자동 갱신
    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }

    // 공지사항 수정 메서드
    fun update(title: String, content: String) {
        this.title = title
        this.content = content
    }


}
