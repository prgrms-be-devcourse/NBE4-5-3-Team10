package com.tripfriend.domain.qna.qna.service

import com.tripfriend.domain.member.member.entity.Member
import com.tripfriend.domain.member.member.repository.MemberRepository
import com.tripfriend.domain.qna.dto.AnswerDto
import com.tripfriend.domain.qna.entity.Answer
import com.tripfriend.domain.qna.repository.AnswerRepository
import com.tripfriend.domain.qna.repository.QuestionRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class AnswerService(
    private val answerRepository: AnswerRepository,
    private val questionRepository: QuestionRepository,
    private val memberRepository: MemberRepository
) {

    //답변 생성
    @Transactional
    fun createAnswer(questionId: Long, memberId: Long, content: String) {
        val question = questionRepository.findById(questionId)
            .orElseThrow { IllegalArgumentException("질문을 찾을 수 없습니다.") }

        val member = memberRepository.findById(memberId)
            .orElseThrow { IllegalArgumentException("회원 정보를 찾을 수 없습니다.") }

        val answer = Answer(
            question = requireNotNull(question),
            member = requireNotNull(member),
            content = content
        )

        answerRepository.save(answer)
    }

    @Transactional
    fun getAnswersByQuestionId(questionId: Long): List<AnswerDto> {
        val question = questionRepository.findById(questionId)
            .orElseThrow { IllegalArgumentException("질문을 찾을 수 없습니다.") }

        return requireNotNull(question).answers.map { AnswerDto(it) }
    }

    //답변 삭제
    @Transactional
    fun deleteAnswer(answerId: Long, currentMember: Member) {
        val answer = answerRepository.findById(answerId)
            .orElseThrow { IllegalArgumentException("답변을 찾을 수 없습니다.") }

        if (answer.member.id != currentMember.id) {
            throw SecurityException("본인의 답변만 삭제할 수 있습니다.")
        }

        answerRepository.delete(answer)
    }

    //관리자 답변 삭제
    @Transactional
    fun deleteAnswerByAdmin(answerId: Long) {
        val answer = answerRepository.findById(answerId)
            .orElseThrow { RuntimeException("해당 답변을 찾을 수 없습니다.") }

        answerRepository.delete(answer)
    }
}
