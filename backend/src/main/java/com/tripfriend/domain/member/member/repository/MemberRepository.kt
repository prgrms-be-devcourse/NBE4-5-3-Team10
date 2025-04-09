package com.tripfriend.domain.member.member.repository

import com.tripfriend.domain.member.member.entity.Member
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime
import java.util.*

interface MemberRepository : JpaRepository<Member, Long> {

    fun existsByUsername(username: String): Boolean

    fun existsByEmail(email: String): Boolean

    fun existsByNickname(nickname: String): Boolean

    fun findByUsername(username: String): Optional<Member>

    fun findByEmail(email: String): Optional<Member>

    // 삭제된 계정만 조회
    fun findByIdAndDeletedTrue(id: Long): Optional<Member>

    // 이메일과 삭제 여부로 조회 (로그인에서 활용)
    fun findByEmailAndDeleted(email: String, deleted: Boolean): Optional<Member>

    // 삭제된 계정 중 특정 기간 내의 계정들 조회
    fun findByDeletedTrueAndDeletedAtAfter(date: LocalDateTime): List<Member>

    // 삭제된 계정 중 해당 날짜 이전에 삭제된 계정들 조회
    fun findByDeletedTrueAndDeletedAtBefore(date: LocalDateTime): List<Member>
}
