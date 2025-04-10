package com.tripfriend.domain.qna.repository;

import com.tripfriend.domain.qna.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query("SELECT q FROM Question q JOIN FETCH q.member")
    List<Question> findAllWithMember();

    @Query("SELECT q FROM Question q JOIN FETCH q.member WHERE q.id = :id")
    Optional<Question> findByIdWithMember(@Param("id") Long id);

    @Query("SELECT q FROM Question q JOIN FETCH q.member LEFT JOIN FETCH q.answers WHERE q.id = :id")
    Optional<Question> findByIdWithAnswers(@Param("id") Long id);

}
