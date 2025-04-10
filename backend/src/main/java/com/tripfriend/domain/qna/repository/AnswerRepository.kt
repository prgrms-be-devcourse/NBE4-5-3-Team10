package com.tripfriend.domain.qna.repository;

import com.tripfriend.domain.qna.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
}
