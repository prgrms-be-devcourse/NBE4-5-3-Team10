package com.tripfriend.domain.qna.repository

import com.tripfriend.domain.qna.entity.Question
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface QuestionRepository : JpaRepository<Question?, Long?> {
    @Query("SELECT q FROM Question q JOIN FETCH q.member")
    fun findAllWithMember(): List<Question>

    @Query("SELECT q FROM Question q JOIN FETCH q.member WHERE q.id = :id")
    fun findByIdWithMember(@Param("id") id: Long): Optional<Question?>?

    @Query("SELECT q FROM Question q JOIN FETCH q.member LEFT JOIN FETCH q.answers WHERE q.id = :id")
    fun findByIdWithAnswers(@Param("id") id: Long): Optional<Question?>?
}
