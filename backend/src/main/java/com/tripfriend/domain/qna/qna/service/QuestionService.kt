package com.tripfriend.domain.qna.qna.service

import com.tripfriend.domain.member.member.entity.Member
import com.tripfriend.domain.member.member.repository.MemberRepository
import com.tripfriend.domain.qna.dto.QuestionDto
import com.tripfriend.domain.qna.dto.QuestionWithAnswersDto
import com.tripfriend.domain.qna.entity.Question
import com.tripfriend.domain.qna.repository.QuestionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
class QuestionService(
    private val questionRepository: QuestionRepository,
    private val memberRepository: MemberRepository
) {

    // 질문 생성
    fun createQuestion(title: String, content: String, member: Member): Question {
        val question = Question(
            title = title,
            content = content,
            member = member
        )
        return questionRepository.save(question)
    }

    // 전체 질문 조회
    fun getAllQuestions(): List<Question> =
        questionRepository.findAllWithMember()?.filterNotNull() ?: emptyList()

    // 특정 질문 조회
    fun getQuestionById(id: Long): Question =
        questionRepository.findByIdWithMember(id)
            ?.orElseThrow { RuntimeException("해당 질문을 찾을 수 없습니다.") }
            ?: throw RuntimeException("질문을 찾을 수 없습니다.")

    // 질문 삭제 (작성자 확인)
    fun deleteQuestionById(id: Long, member: Member) {
        val question = getQuestionById(id)

        if (question.member.id != member.id) {
            throw SecurityException("작성자만 질문을 삭제할 수 있습니다.")
        }

        questionRepository.delete(question)
    }

    // 관리자 전체 질문 조회
    fun getAllQuestionsForAdmin(): List<QuestionDto> =
        questionRepository.findAllWithMember()
            ?.filterNotNull()
            ?.map { QuestionDto(it) }
            ?: emptyList()

    // 관리자 질문 삭제
    @Transactional
    fun deleteQuestionByAdmin(id: Long) {
        val question = questionRepository.findByIdWithAnswers(id)
            ?.orElseThrow { RuntimeException("해당 질문을 찾을 수 없습니다.") }
            ?: throw RuntimeException("질문을 찾을 수 없습니다.")

        questionRepository.delete(question)
    }

    // 질문 + 답변 목록 조회
    @Transactional
    fun getQuestionWithAnswers(id: Long): QuestionWithAnswersDto {
        val question = questionRepository.findByIdWithAnswers(id)
            ?.orElseThrow { RuntimeException("해당 질문을 찾을 수 없습니다.") }
            ?: throw RuntimeException("질문을 찾을 수 없습니다.")

        return QuestionWithAnswersDto(question)
    }
}
